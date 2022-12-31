package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.ReceiptQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ReceiptService {

    private final ReceiptRepository repository;

    @Autowired
    public ReceiptService(ReceiptRepository repository) {
        this.repository = repository;
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

    public Single<Receipt> get(Long id) {
        return RxJava3Adapter.monoToMaybe(find(id))
                .switchIfEmpty(Single.error(new RuntimeException("Receipt not found")));
    }

    public Mono<Receipt> save(Receipt receipt) {
        return repository.save(receipt);
    }

    public Mono<Long> countAll() {
        return repository.count();
    }


}
