package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.core.util.ObjectUtil;
import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SaveReceipt {

    private final ReceiptService receiptService;
    private final SequenceService sequenceService;
    private final BuildingService buildingService;
    private final ApartmentService apartmentService;

    @Autowired
    public SaveReceipt(ReceiptService receiptService, SequenceService sequenceService, BuildingService buildingService, ApartmentService apartmentService) {
        this.receiptService = receiptService;
        this.sequenceService = sequenceService;
        this.buildingService = buildingService;
        this.apartmentService = apartmentService;
    }

    public Single<Receipt> save(Receipt receipt) {


        final var nextSequence = Maybe.fromOptional(Optional.ofNullable(receipt.id()))
                .switchIfEmpty(sequenceService.rxNextSequence(Sequence.Type.RECEIPT));

        final var apartmentsByBuilding = apartmentService.rxApartmentsByBuilding(receipt.buildingId());

        final var buildingSingle = buildingService.get(receipt.buildingId());

        return Single.zip(nextSequence, apartmentsByBuilding, buildingSingle, (id, apartments, building) -> {

                    final var expenses = receipt.expenses().stream()
                            .filter(expense -> !expense.description().equals("DIFERENCIA DE ALIQUOTA"))
                            .collect(Collectors.toCollection(LinkedList::new));

                    final var totalCommonExpensePair = ConvertUtil.pair(expenses, r -> r.type() == Expense.Type.COMMON, receipt.rate().rate());
                    final var totalCommonExpenses = totalCommonExpensePair.getFirst();

                    final var aliquotDifference = aliquotDifference(apartments, totalCommonExpenses);

                    expenses.add(Expense.builder()
                            .description("DIFERENCIA DE ALIQUOTA")
                            .amount(aliquotDifference)
                            .currency(totalCommonExpensePair.getSecond())
                            .type(Expense.Type.UNCOMMON)
                            .build());

                    final var totalUnCommonExpensePair = ConvertUtil.pair(expenses, r -> r.type() == Expense.Type.UNCOMMON, receipt.rate().rate());

                    final var debtReceiptsAmount = receipt.debts().stream().map(Debt::receipts)
                            .reduce(Integer::sum)
                            .orElse(0);

                    final var debtTotal = receipt.debts().stream().map(Debt::amount)
                            .reduce(BigDecimal::add)
                            .orElse(BigDecimal.ZERO);

                    final var unCommonPay = DecimalUtil.greaterThanZero(totalUnCommonExpensePair.getFirst()) ? totalUnCommonExpensePair.getFirst().divide(BigDecimal.valueOf(apartments.size()), MathContext.DECIMAL128) : BigDecimal.ZERO;

                    final var aptTotals = apartments.stream()
                            .map(apartment -> {

                                final var extraCharges = ObjectUtil.extraCharges(apartment.apartmentId().number(), building.extraCharges(), receipt.extraCharges());

                                final var pay = ObjectUtil.totalAptPay(unCommonPay, ObjectUtil.aBoolean(building.fixedPay()), building.fixedPayAmount(), building.mainCurrency(), receipt.rate().rate(), totalCommonExpenses,
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
                            .build();
                })
                .flatMap(receiptService::save);
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
