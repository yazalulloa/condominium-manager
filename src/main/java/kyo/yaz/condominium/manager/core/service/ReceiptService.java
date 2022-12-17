package kyo.yaz.condominium.manager.core.service;

import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.ReceiptQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.repository.ReceiptRepository;
import kyo.yaz.condominium.manager.ui.views.domain.ReceiptViewItem;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ReceiptService {

    private final ReceiptRepository repository;
    private final SequenceService sequenceService;

    @Autowired
    public ReceiptService(ReceiptRepository repository, SequenceService sequenceService) {
        this.repository = repository;
        this.sequenceService = sequenceService;
    }

    public Mono<List<ReceiptViewItem>> list(String buildingId, String filter, int page, int pageSize) {

        final var sortings = new LinkedHashSet<Sorting<ReceiptQueryRequest.SortField>>();
        sortings.add(ReceiptQueryRequest.sorting(ReceiptQueryRequest.SortField.ID, Sort.Direction.DESC));

        final var request = ReceiptQueryRequest.builder()
                .buildingId(buildingId)
                .expense(filter)
                .page(PageRequest.of(page, pageSize))
                .sortings(sortings)
                .build();

        return repository.find(request)
                .map(ConvertUtil::receipt)
                .collectList();
    }

    public Mono<Void> delete(Long id) {
        return repository.deleteById(id);
    }

    public Mono<Void> delete(Receipt entity) {

        return repository.delete(entity);
    }

    public Mono<Receipt> save(Receipt receipt) {
        return repository.save(receipt);
    }

    public Mono<Long> countAll() {
        return repository.count();
    }
}
