package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.RateQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.persistence.repository.RateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RateService {

    private final RateRepository repository;

    @Autowired
    public RateService(RateRepository repository) {
        this.repository = repository;
    }

    public Mono<Paging<Rate>> paging(int page, int pageSize) {
        final var sortings = new LinkedHashSet<Sorting<RateQueryRequest.SortField>>();
        sortings.add(RateQueryRequest.sorting(RateQueryRequest.SortField.ID, Sort.Direction.DESC));
        sortings.add(RateQueryRequest.sorting(RateQueryRequest.SortField.DATE_OF_RATE, Sort.Direction.DESC));
        sortings.add(RateQueryRequest.sorting(RateQueryRequest.SortField.CREATED_AT, Sort.Direction.DESC));

        final var request = RateQueryRequest.builder()
                .page(PageRequest.of(page, pageSize))
                .sortings(sortings)
                .build();

        return paging(request);
    }

    public Mono<Paging<Rate>> paging(RateQueryRequest request) {
        final var listMono = repository.list(request);
        final var totalCountMono = countAll();
        final var queryCountMono = repository.count(request);

        return Mono.zip(totalCountMono, queryCountMono, listMono)
                .map(tuple -> new Paging<>(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    public Mono<Void> delete(Rate entity) {
        return repository.delete(entity);
    }

    public Mono<Rate> save(Rate entity) {
        return repository.save(entity);
    }

    public Mono<Long> countAll() {
        return repository.count();
    }

    public Mono<Rate> last(Currency fromCurrency, Currency toCurrency) {


        final var sortings = new LinkedHashSet<Sorting<RateQueryRequest.SortField>>();
        sortings.add(RateQueryRequest.sorting(RateQueryRequest.SortField.ID, Sort.Direction.DESC));
        sortings.add(RateQueryRequest.sorting(RateQueryRequest.SortField.DATE_OF_RATE, Sort.Direction.DESC));
        sortings.add(RateQueryRequest.sorting(RateQueryRequest.SortField.CREATED_AT, Sort.Direction.DESC));

        final var request = RateQueryRequest.builder()
                .fromCurrency(Set.of(fromCurrency))
                .toCurrency(Set.of(toCurrency))
                .page(PageRequest.of(0, 1))
                .sortings(sortings)
                .build();

        return repository.list(request)
                .map(List::iterator)
                .filter(Iterator::hasNext)
                .map(Iterator::next);
    }

    public Single<Rate> getLast(Currency fromCurrency, Currency toCurrency) {
        return RxJava3Adapter.monoToMaybe(last(fromCurrency, toCurrency))
                .switchIfEmpty(Single.error(new RuntimeException("Last rate not found")));
    }
}
