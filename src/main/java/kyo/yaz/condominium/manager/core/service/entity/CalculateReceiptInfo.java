package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.core.util.ObjectUtil;
import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.persistence.domain.ExtraCharge;
import kyo.yaz.condominium.manager.persistence.domain.ReserveFund;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.entity.Receipt.ReserveFundTotal;
import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculateReceiptInfo {

  private final ReceiptService receiptService;
  private final SequenceService sequenceService;
  private final BuildingService buildingService;
  private final ApartmentService apartmentService;

  public Single<Receipt> save(Receipt receipt) {

    return calculate(receipt)
        .flatMap(receiptService::save);
  }

  public Receipt calculate(Receipt receipt, Building building, List<Apartment> apartments) {
    final var expenses = receipt.expenses().stream()
        .filter(expense -> !expense.description().equals("DIFERENCIA DE ALIQUOTA"))
        .filter(expense -> !expense.reserveFund())
        .collect(Collectors.toCollection(LinkedList::new));

    final var totalCommonExpensePair = ConvertUtil.pair(expenses,
        r -> r.type() == Expense.Type.COMMON && !r.reserveFund(), receipt.rate().rate());
    final var totalCommonExpensesBeforeReserveFund = totalCommonExpensePair.getFirst();

    final var debtReceiptsAmount = receipt.debts().stream().map(Debt::receipts)
        .reduce(Integer::sum)
        .orElse(0);

    final var debtTotal = receipt.debts().stream().map(Debt::amount)
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);

    final var reserveFundTotals = Optional.ofNullable(building.reserveFunds())
        .orElseGet(Collections::emptyList)
        .stream()
        .filter(reserveFund -> reserveFund.getActive() && DecimalUtil.greaterThanZero(reserveFund.getPay()))
        .map(reserveFund -> {

          final var amount = reserveFund.getType() == ReserveFund.Type.FIXED_PAY ? reserveFund.getPay() :
              DecimalUtil.percentageOf(reserveFund.getPay(), totalCommonExpensesBeforeReserveFund);

          return Receipt.ReserveFundTotal.builder()
              .name(reserveFund.getName())
              .fund(reserveFund.getFund())
              .expense(reserveFund.getExpense())
              .amount(amount)
              .type(reserveFund.getType())
              .expenseType(reserveFund.getExpenseType())
              .pay(reserveFund.getPay())
              .addToExpenses(reserveFund.getAddToExpenses())
              .build();
        })
        .toList();

    final var totalCommonExpenses = reserveFundTotals.stream()
        .filter(ReserveFundTotal::addToExpenses)
        .filter(res -> res.expenseType() == Expense.Type.COMMON)
        .map(Receipt.ReserveFundTotal::amount)
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO)
        .add(totalCommonExpensesBeforeReserveFund);

    reserveFundTotals.stream().filter(ReserveFundTotal::addToExpenses).map(fund -> {
      final var isFixedPay = fund.type() == ReserveFund.Type.FIXED_PAY;
      return Expense.builder()
          .description(fund.name() + " " + fund.pay() + (isFixedPay ? "" : "%"))
          .amount(fund.amount())
          .currency(totalCommonExpensePair.getSecond())
          .type(fund.expenseType())
          .reserveFund(true)
          .build();
    }).forEach(expenses::add);

    final var aliquotDifference = aliquotDifference(apartments, totalCommonExpenses);

    expenses.add(Expense.builder()
        .description("DIFERENCIA DE ALIQUOTA")
        .amount(aliquotDifference)
        .currency(totalCommonExpensePair.getSecond())
        .type(Expense.Type.UNCOMMON)
        .build());

    final var totalUnCommonExpensePair = ConvertUtil.pair(expenses, r -> r.type() == Expense.Type.UNCOMMON,
        receipt.rate().rate());

    final var totalUnCommonExpenses = totalUnCommonExpensePair.getFirst();

    final var equalsToZero = DecimalUtil.equalsToZero(totalUnCommonExpenses);
    final var unCommonPay =
        equalsToZero ? BigDecimal.ZERO : totalUnCommonExpenses
            .divide(BigDecimal.valueOf(apartments.size()), MathContext.DECIMAL128);

    final var aptTotals = apartments.stream()
        .map(apartment -> {

          final var extraCharges = extraCharges(apartment.apartmentId().number(), building.extraCharges(),
              receipt.extraCharges());

          final var amounts = totalAptPay(unCommonPay, building, receipt.rate().rate(), totalCommonExpenses,
              apartment.amountToPay(), extraCharges);

          return Receipt.AptTotal.builder()
              .number(apartment.apartmentId().number())
              .name(apartment.name())
              //.amount(amounts.get(Currency.USD))
              .amounts(amounts)
              .extraCharges(extraCharges)
              .build();

        })
        .collect(Collectors.toCollection(LinkedList::new));

    return receipt.toBuilder()
        .expenses(expenses)
        .totalCommonExpenses(totalCommonExpenses)
        .totalCommonExpensesCurrency(totalCommonExpensePair.getSecond())
        .totalUnCommonExpenses(totalUnCommonExpenses)
        .totalUnCommonExpensesCurrency(totalUnCommonExpensePair.getSecond())
        .totalDebt(debtTotal)
        .debtReceiptsAmount(debtReceiptsAmount)
        .createdAt(Optional.ofNullable(receipt.createdAt()).orElseGet(DateUtil::nowZonedWithUTC))
        .updatedAt(receipt.createdAt() != null ? DateUtil.nowZonedWithUTC() : null)
        .aptTotals(aptTotals)
        .reserveFundTotals(reserveFundTotals)
        .build();

  }

  public Single<Receipt> calculate(Receipt receipt) {
    final var nextSequence = Maybe.fromOptional(Optional.ofNullable(receipt.id()))
        .switchIfEmpty(sequenceService.nextSequence(Sequence.Type.RECEIPT));

    final var apartmentsByBuilding = apartmentService.rxApartmentsByBuilding(receipt.buildingId());

    final var buildingSingle = buildingService.get(receipt.buildingId());

    return Single.zip(nextSequence, apartmentsByBuilding, buildingSingle, (id, apartments, building) -> {

      return calculate(receipt, building, apartments)
          .toBuilder()
          .id(id)
          .build();
    });
  }

  public Single<Receipt> calculate(long id) {
    return receiptService.find(id)
        .toSingle()
        .flatMap(this::calculate);
  }

  private BigDecimal aliquotDifference(Collection<Apartment> list, BigDecimal totalCommonExpenses) {

    if (DecimalUtil.equalsToZero(totalCommonExpenses)) {
      return BigDecimal.ZERO;
    }

    final var totalAliquot = list.stream()
        .map(Apartment::amountToPay)
        .map(aliquot -> DecimalUtil.percentageOf(aliquot, totalCommonExpenses))
        .reduce(BigDecimal::add)
        .orElseThrow(() -> new RuntimeException("NO_ALIQUOT_FOUND"));

    final var aliquoutDifference = totalAliquot.subtract(totalCommonExpenses);

    if (DecimalUtil.greaterThanZero(aliquoutDifference)) {
      return aliquoutDifference;
    }

    return BigDecimal.ZERO;
  }

  private Map<Currency, BigDecimal> totalAptPay(BigDecimal unCommonPayPerApt, Building building, BigDecimal rate,
      BigDecimal totalCommonExpenses,
      BigDecimal aptAliquot,
      Collection<ExtraCharge> extraCharges) {

    final var currency = building.mainCurrency();
    if (ObjectUtil.aBoolean(building.fixedPay())) {

      return totalPayment(building.fixedPay(), building.fixedPayAmount(), currency, rate, extraCharges);

    } else {

      //document.add(new Paragraph("MONTO DE GASTOS NO COMUNES POR C/U: " + currencyType.numberFormat().format(unCommonPay)));

      final var aliquotAmount = DecimalUtil.percentageOf(aptAliquot, totalCommonExpenses);
      // document.add(new Paragraph("MONTO POR ALIQUOTA: " + currencyType.numberFormat().format(aliquotAmount)));
      final var beforePay = aliquotAmount.add(unCommonPayPerApt);//.setScale(2, RoundingMode.UP);
      return totalPayment(building.fixedPay(), beforePay, currency, rate, extraCharges);

    }
  }

  private Map<Currency, BigDecimal> totalPayment(boolean fixedPay,
      BigDecimal preCalculatedPayment,
      Currency currencyType, BigDecimal usdExchangeRate,
      Collection<ExtraCharge> extraCharges) {

    var usdPay = BigDecimal.ZERO;
    var vesPay = BigDecimal.ZERO;

    Function<BigDecimal, BigDecimal> toUsd = ves -> {
      return ves.divide(usdExchangeRate, 2, RoundingMode.HALF_UP);
    };

    Function<BigDecimal, BigDecimal> toVes = usd -> {
      return usd.multiply(usdExchangeRate);
    };

    final var vesExtraCharge = extraCharges.stream().filter(c -> c.currency() == Currency.VED)
        .map(ExtraCharge::amount)
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);

    final var usdExtraCharge = extraCharges.stream().filter(c -> c.currency() == Currency.USD)
        .map(ExtraCharge::amount)
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);

    vesPay = vesPay.add(vesExtraCharge)
        .add(DecimalUtil.equalsToZero(usdExtraCharge) ? BigDecimal.ZERO : toVes.apply(usdExtraCharge));
    usdPay = usdPay.add(usdExtraCharge)
        .add(DecimalUtil.equalsToZero(vesExtraCharge) ? BigDecimal.ZERO : toUsd.apply(vesExtraCharge));

    if (fixedPay) {
      if (currencyType == Currency.USD) {
        usdPay = usdPay.add(preCalculatedPayment);
        vesPay = vesPay.add(toVes.apply(preCalculatedPayment));
      } else {
        vesPay = vesPay.add(preCalculatedPayment);
        usdPay = usdPay.add(toUsd.apply(preCalculatedPayment));
      }
    } else {
      vesPay = vesPay.add(preCalculatedPayment);
      usdPay = usdPay.add(toUsd.apply(preCalculatedPayment));
    }

    Function<BigDecimal, BigDecimal> function = bigDecimal -> {
            /*if (building.roundUpPayments()) {
                return bigDecimal.setScale(0, RoundingMode.UP);
            }*/

      return bigDecimal.setScale(2, RoundingMode.HALF_UP);
    };

    return Map.of(
        Currency.USD, function.apply(usdPay),
        Currency.VED, function.apply(vesPay)
    );
  }

  private List<ExtraCharge> extraCharges(String aptNumber, List<ExtraCharge> first, List<ExtraCharge> second) {

    final var receiptCharges = Optional.ofNullable(first)
        .orElseGet(Collections::emptyList)
        .stream()
        .filter(extraCharge -> extraCharge.aptNumber().equals(aptNumber))
        .filter(extraCharge -> DecimalUtil.greaterThanZero(extraCharge.amount()));

    final var buildingCharges = Optional.ofNullable(second)
        .orElseGet(Collections::emptyList)
        .stream()
        .filter(extraCharge -> extraCharge.aptNumber().equals(aptNumber))
        .filter(extraCharge -> DecimalUtil.greaterThanZero(extraCharge.amount()));

    return Stream.concat(receiptCharges, buildingCharges).toList();
  }
}
