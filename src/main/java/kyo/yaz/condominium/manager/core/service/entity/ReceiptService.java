package kyo.yaz.condominium.manager.core.service.entity;

import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.persistence.domain.IAmountCurrency;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.ReceiptQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import kyo.yaz.condominium.manager.persistence.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class ReceiptService {

    private final ReceiptRepository repository;
    private final SequenceService sequenceService;

    @Autowired
    public ReceiptService(ReceiptRepository repository, SequenceService sequenceService) {
        this.repository = repository;
        this.sequenceService = sequenceService;
    }

    public Mono<List<Receipt>> list(String buildingId, String filter, int page, int pageSize) {

        final var sortings = new LinkedHashSet<Sorting<ReceiptQueryRequest.SortField>>();
        sortings.add(ReceiptQueryRequest.sorting(ReceiptQueryRequest.SortField.ID, Sort.Direction.DESC));

        final var request = ReceiptQueryRequest.builder()
                .buildingId(buildingId)
                .expense(filter)
                .page(PageRequest.of(page, pageSize))
                .sortings(sortings)
                .build();

        return repository.list(request);
    }

    public Mono<Void> delete(Long id) {
        return repository.deleteById(id);
    }

    public Mono<Void> delete(Receipt entity) {

        return repository.delete(entity);
    }

    public Mono<Receipt> find(Long id) {
        return repository.findById(id);
    }

    public Mono<Receipt> save(Receipt receipt) {

        final var nextSequence = sequenceService.nextSequence(Sequence.Type.RECEIPT);

        return Mono.justOrEmpty(receipt.id())
                .switchIfEmpty(nextSequence)
                .map(id -> {


                    final var totalCommonExpensePair = pair(receipt.expenses(), r -> r.type() == Expense.Type.COMMON, receipt.rate().rate());
                    final var totalUnCommonExpensePair = pair(receipt.expenses(), r -> r.type() == Expense.Type.UNCOMMON, receipt.rate().rate());

                    final var totalDebtPair = pair(receipt.debts(), d -> true, receipt.rate().rate());

                    final var debtReceiptsAmount = receipt.debts().stream().map(Debt::receipts)
                            .reduce(Integer::sum)
                            .orElse(0);

                    return receipt.toBuilder()
                            .id(id)
                            .totalCommonExpenses(totalCommonExpensePair.getFirst())
                            .totalCommonExpensesCurrency(totalCommonExpensePair.getSecond())
                            .totalUnCommonExpenses(totalUnCommonExpensePair.getFirst())
                            .totalUnCommonExpensesCurrency(totalUnCommonExpensePair.getSecond())
                            .totalDebt(totalDebtPair.getFirst())
                            .totalDebtCurrency(totalDebtPair.getSecond())
                            .debtReceiptsAmount(debtReceiptsAmount)
                            .createdAt(Optional.ofNullable(receipt.createdAt()).orElseGet(DateUtil::nowZonedWithUTC))
                            .updatedAt(receipt.createdAt() != null ? DateUtil.nowZonedWithUTC() : null)
                            .build();
                })
                .flatMap(repository::save);
    }

    public Mono<Long> countAll() {
        return repository.count();
    }

    private <T extends IAmountCurrency> Pair<BigDecimal, Currency> pair(Collection<T> collection, Predicate<T> predicate, BigDecimal usdRate) {


        final var usdAmount = collection.stream().filter(predicate)
                .filter(o -> o.currency() == Currency.USD)
                .map(IAmountCurrency::amount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        final var vedAmount = collection.stream().filter(predicate)
                .filter(o -> o.currency() == Currency.VED)
                .map(IAmountCurrency::amount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);


        if (DecimalUtil.greaterThanZero(vedAmount)) {
            final var amount = usdAmount.multiply(usdRate)
                    .add(vedAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            return Pair.of(amount, Currency.VED);
        }

        return Pair.of(usdAmount, Currency.USD);
    }
}
