package kyo.yaz.condominium.manager.core.service;

import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.ReceiptQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import kyo.yaz.condominium.manager.persistence.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

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
                .map(id -> receipt.toBuilder()
                        .id(id)
                        .createdAt(Optional.ofNullable(receipt.createdAt()).orElseGet(DateUtil::nowZonedWithUTC))
                        .updatedAt(Optional.ofNullable(receipt.updatedAt()).orElseGet(DateUtil::nowZonedWithUTC))
                        .build())
                .flatMap(repository::save);
    }

    public Mono<Long> countAll() {
        return repository.count();
    }
}
