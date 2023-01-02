package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.core.util.ObjectUtil;
import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;

import java.math.BigDecimal;
import java.math.MathContext;
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
                    final var totalCommonExpensePair = ConvertUtil.pair(receipt.expenses(), r -> r.type() == Expense.Type.COMMON, receipt.rate().rate());
                    final var totalUnCommonExpensePair = ConvertUtil.pair(receipt.expenses(), r -> r.type() == Expense.Type.UNCOMMON, receipt.rate().rate());


                    final var debtReceiptsAmount = receipt.debts().stream().map(Debt::receipts)
                            .reduce(Integer::sum)
                            .orElse(0);

                    final var debtTotal = receipt.debts().stream().map(Debt::amount)
                            .reduce(BigDecimal::add)
                            .orElse(BigDecimal.ZERO);

                    final var unCommonPay = receipt.totalUnCommonExpenses().divide(BigDecimal.valueOf(apartments.size()), MathContext.DECIMAL128);

                    final var aptTotals = apartments.stream()
                            .map(apartment -> {

                                final var extraCharges = ObjectUtil.extraCharges(apartment.apartmentId().number(), building.extraCharges(), receipt.extraCharges());

                                final var pay = ObjectUtil.totalAptPay(unCommonPay, ObjectUtil.aBoolean(building.fixedPay()), building.fixedPayAmount(), building.mainCurrency(), receipt.rate().rate(), receipt.totalCommonExpenses(),
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
                            .totalCommonExpenses(totalCommonExpensePair.getFirst())
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
                .map(receiptService::save)
                .flatMap(RxJava3Adapter::monoToSingle);
    }
}