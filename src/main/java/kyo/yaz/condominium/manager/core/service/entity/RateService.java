package kyo.yaz.condominium.manager.core.service.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.domain.FileResponse;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.service.paging.MongoServicePagingProcessorImpl;
import kyo.yaz.condominium.manager.core.service.paging.PagingJsonFile;
import kyo.yaz.condominium.manager.core.service.paging.WriteEntityToFile;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.RateQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.persistence.repository.RateRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Stream;

@Service("RATES")
@AllArgsConstructor
@Slf4j
public class RateService implements MongoService<Rate> {

    private final RateRepository repository;
    private final ObjectMapper objectMapper;

    public Single<Paging<Rate>> paging(int page, int pageSize) {
        final var sortings = new LinkedHashSet<Sorting<RateQueryRequest.SortField>>();
        sortings.add(RateQueryRequest.sorting(RateQueryRequest.SortField.ID, Sort.Direction.DESC));
        sortings.add(RateQueryRequest.sorting(RateQueryRequest.SortField.DATE_OF_RATE, Sort.Direction.DESC));
        sortings.add(RateQueryRequest.sorting(RateQueryRequest.SortField.CREATED_AT, Sort.Direction.DESC));

        final var request = RateQueryRequest.builder()
                .page(PageRequest.of(page, pageSize))
                .sortings(sortings)
                .build();

        return RxJava3Adapter.monoToSingle(paging(request));
    }

    private Mono<Paging<Rate>> paging(RateQueryRequest request) {
        final var listMono = repository.list(request);
        final var totalCountMono = repository.count();
        final var queryCountMono = repository.count(request);

        return Mono.zip(totalCountMono, queryCountMono, listMono)
                .map(tuple -> new Paging<>(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    public Completable delete(Set<Long> longs) {
        final var voidMono = repository.deleteAllById(longs);
        return RxJava3Adapter.monoToCompletable(voidMono);
    }

    public Completable delete(Rate entity) {
        return RxJava3Adapter.monoToCompletable(repository.delete(entity));
    }

    public Single<Rate> save(Rate entity) {
        return RxJava3Adapter.monoToSingle(repository.save(entity));
    }

    public Single<Long> countAll() {
        return RxJava3Adapter.monoToSingle(repository.count());
    }

    public Single<List<Rate>> list(RateQueryRequest request) {
        return RxJava3Adapter.monoToSingle(repository.list(request));
    }

    public Single<Boolean> exists(long hash) {
        final var request = RateQueryRequest.builder()
                .hashes(Set.of(hash))
                .page(PageRequest.of(0, 1))
                .build();

        return list(request)
                .map(List::iterator)
                .map(Iterator::hasNext);
    }

    public Maybe<Rate> last(Currency fromCurrency, Currency toCurrency) {

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

        return list(request)
                .map(List::iterator)
                .filter(Iterator::hasNext)
                .map(Iterator::next);
    }

    public Single<Rate> getLast(Currency fromCurrency, Currency toCurrency) {
        return last(fromCurrency, toCurrency)
                .switchIfEmpty(Single.error(new RuntimeException("Last rate not found")));
    }

    public Stream<Rate> stream(RateQueryRequest request) {
        return repository.stream(request);
    }

    public long count() {
        return Optional.ofNullable(repository.count()
                        .block())
                .orElse(0L);
    }


    public Stream<Rate> stream(Query<Rate, String> query) {

        final var sortings = new LinkedHashSet<Sorting<RateQueryRequest.SortField>>();
        sortings.add(RateQueryRequest.sorting(RateQueryRequest.SortField.ID, Sort.Direction.DESC));
        sortings.add(RateQueryRequest.sorting(RateQueryRequest.SortField.DATE_OF_RATE, Sort.Direction.DESC));
        sortings.add(RateQueryRequest.sorting(RateQueryRequest.SortField.CREATED_AT, Sort.Direction.DESC));

        final var request = RateQueryRequest.builder()
                .page(PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .sortings(sortings)
                .build();

        return stream(request);
    }

    public Single<List<Rate>> list(int page, int pageSize) {
        final var mono = repository.findAllBy(PageRequest.of(page, pageSize))
                .collectList();

        return RxJava3Adapter.monoToSingle(mono);
    }

    public Single<FileResponse> download() {

        final var writeEntityToFile = new WriteEntityToFile<>(objectMapper,
                new MongoServicePagingProcessorImpl<>(this, 20));

        return writeEntityToFile.downloadFile("rates.json.gz");

    }

    public Single<List<Rate>> save(Iterable<Rate> entities) {

        final var mono = repository.saveAll(entities)
                .collectList();

        return RxJava3Adapter.monoToSingle(mono);
    }

    public Completable upload(String fileName) {
        return PagingJsonFile.pagingJsonFile(30, fileName, objectMapper, Rate.class, c -> save(c).ignoreElement());
    }
}
