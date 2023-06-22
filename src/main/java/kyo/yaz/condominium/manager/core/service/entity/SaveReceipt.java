package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.core.util.ObjectUtil;
import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.persistence.domain.ReserveFund;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.entity.Receipt.ReserveFundTotal;
import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaveReceipt {

  private final ReceiptService receiptService;
  private final SequenceService sequenceService;
  private final BuildingService buildingService;
  private final ApartmentService apartmentService;

  public Single<Receipt> save(Receipt receipt) {

    return calculate(receipt)
        .flatMap(receiptService::save);
  }

  public Single<Receipt> calculate(Receipt receipt) {
    final var nextSequence = Maybe.fromOptional(Optional.ofNullable(receipt.id()))
        .switchIfEmpty(sequenceService.nextSequence(Sequence.Type.RECEIPT));

    final var apartmentsByBuilding = apartmentService.rxApartmentsByBuilding(receipt.buildingId());

    final var buildingSingle = buildingService.get(receipt.buildingId());

    return Single.zip(nextSequence, apartmentsByBuilding, buildingSingle, (id, apartments, building) -> {

      final var expenses = receipt.expenses().stream()
          .filter(expense -> !expense.description().equals("DIFERENCIA DE ALIQUOTA"))
          .collect(Collectors.toCollection(LinkedList::new));

      final var totalCommonExpensePair = ConvertUtil.pair(expenses,
          r -> r.type() == Expense.Type.COMMON && !r.reserveFund(), receipt.rate().rate());
      final var totalCommonExpensesBeforeReserveFund = totalCommonExpensePair.getFirst();

      final var totalUnCommonExpensePair = ConvertUtil.pair(expenses, r -> r.type() == Expense.Type.UNCOMMON,
          receipt.rate().rate());

      final var debtReceiptsAmount = receipt.debts().stream().map(Debt::receipts)
          .reduce(Integer::sum)
          .orElse(0);

      final var debtTotal = receipt.debts().stream().map(Debt::amount)
          .reduce(BigDecimal::add)
          .orElse(BigDecimal.ZERO);

      final var unCommonPay =
          DecimalUtil.greaterThanZero(totalUnCommonExpensePair.getFirst()) ? totalUnCommonExpensePair.getFirst()
              .divide(BigDecimal.valueOf(apartments.size()), MathContext.DECIMAL128) : BigDecimal.ZERO;

      final var reserveFundTotals = Optional.ofNullable(building.reserveFunds())
          .orElseGet(Collections::emptyList)
          .stream()
          .filter(reserveFund -> reserveFund.active() && DecimalUtil.greaterThanZero(reserveFund.pay()))
          .map(reserveFund -> {

            final var amount = reserveFund.type() == ReserveFund.Type.FIXED_PAY ? reserveFund.pay() :
                DecimalUtil.percentageOf(reserveFund.pay(), totalCommonExpensesBeforeReserveFund);

            return Receipt.ReserveFundTotal.builder()
                .name(reserveFund.name())
                .fund(reserveFund.fund())
                .amount(amount)
                .type(reserveFund.type())
                .expenseType(reserveFund.expenseType())
                .pay(reserveFund.pay())
                .addToExpenses(reserveFund.addToExpenses())
                .build();
          })
          .toList();

      final var totalCommonExpenses = reserveFundTotals.stream().filter(ReserveFundTotal::addToExpenses)
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

      final var aptTotals = apartments.stream()
          .map(apartment -> {

            final var extraCharges = ObjectUtil.extraCharges(apartment.apartmentId().number(), building.extraCharges(),
                receipt.extraCharges());

            final var pay = ObjectUtil.totalAptPay(unCommonPay, building, receipt.rate().rate(), totalCommonExpenses,
                apartment.amountToPay(), extraCharges);

            return Receipt.AptTotal.builder()
                .number(apartment.apartmentId().number())
                .name(apartment.name())
                .amount(pay)
                .extraCharges(extraCharges)
                .build();

          })
          .collect(Collectors.toCollection(LinkedList::new));

      return receipt.toBuilder()
          .id(id)
          .expenses(expenses)
          .totalCommonExpenses(totalCommonExpenses)
          .totalCommonExpensesCurrency(totalCommonExpensePair.getSecond())
          .totalUnCommonExpenses(totalUnCommonExpensePair.getFirst())
          .totalUnCommonExpensesCurrency(totalUnCommonExpensePair.getSecond())
          .totalDebt(debtTotal)
          .debtReceiptsAmount(debtReceiptsAmount)
          .createdAt(Optional.ofNullable(receipt.createdAt()).orElseGet(DateUtil::nowZonedWithUTC))
          .updatedAt(receipt.createdAt() != null ? DateUtil.nowZonedWithUTC() : null)
          .aptTotals(aptTotals)
          .reserveFundTotals(reserveFundTotals)
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
}
