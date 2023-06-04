package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.ApartmentQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.repository.ApartmentRepository;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@Slf4j
public class ApartmentService {

    private final ApartmentRepository repository;


    @Autowired
    public ApartmentService(ApartmentRepository repository) {
        this.repository = repository;
    }

    public Single<Apartment> read(String buildingId, String aptNumber) {
        return RxJava3Adapter.monoToSingle(repository.findById(Apartment.ApartmentId.of(buildingId, aptNumber)));
    }

    public Single<Paging<Apartment>> paging(Set<String> buildings, String filter, int page, int pageSize) {

        final var sortings = new LinkedHashSet<Sorting<ApartmentQueryRequest.SortField>>();

        if (buildings == null || buildings.isEmpty()) {
            sortings.add(ApartmentQueryRequest.sorting(ApartmentQueryRequest.SortField.BUILDING_ID, Sort.Direction.ASC));
        }

        sortings.add(ApartmentQueryRequest.sorting(ApartmentQueryRequest.SortField.NUMBER, Sort.Direction.ASC));

        final var request = ApartmentQueryRequest.builder()
                .buildings(buildings)
                .apartment(filter)
                .page(PageRequest.of(page, pageSize))
                .sortings(sortings)
                .build();

        return RxJava3Adapter.monoToSingle(paging(request));
    }

    private Mono<Paging<Apartment>> paging(ApartmentQueryRequest request) {
        final var listMono = repository.list(request);
        final var totalCountMono = repository.count();
        final var queryCountMono = repository.count(request);

        return Mono.zip(totalCountMono, queryCountMono, listMono)
                .map(tuple -> new Paging<>(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    public Single<List<Apartment>> rxApartmentsByBuilding(String buildingId) {
        return apartmentsByBuilding(buildingId);
    }

    public Single<List<Apartment>> apartmentsByBuilding(String buildingId) {
        final var sortings = new LinkedHashSet<Sorting<ApartmentQueryRequest.SortField>>();
        sortings.add(ApartmentQueryRequest.sorting(ApartmentQueryRequest.SortField.NUMBER, Sort.Direction.ASC));
        final var request = ApartmentQueryRequest.builder()
                .buildings(Collections.singleton(buildingId))
                .sortings(sortings)
                .build();

        return RxJava3Adapter.monoToSingle(repository.list(request))
                .flatMapObservable(Observable::fromIterable)
                .sorted(Comparator.comparing((Apartment a) -> a.apartmentId().buildingId())
                        .thenComparing(ConvertUtil.aptNumberComparator()))
                .toList(LinkedList::new);
    }

    public Single<List<Apartment>> aptNumbers(String buildingId) {
        final var mono = repository.getAptNumberName(buildingId)
                .sort(Comparator.comparing(a -> a.apartmentId().number()))
                .collectList();
        return RxJava3Adapter.monoToSingle(mono);
    }

    public Completable delete(Apartment entity) {
        return RxJava3Adapter.monoToCompletable(repository.delete(entity));
    }

    public Single<Apartment> save(Apartment entity) {
        return RxJava3Adapter.monoToSingle(repository.save(entity));
    }

    public Single<Long> countAll() {
        return RxJava3Adapter.monoToSingle(repository.count());
    }

    public Single<Long> countByBuilding(String buildingId) {
        final var request = ApartmentQueryRequest.builder()
                .buildings(Collections.singleton(buildingId))
                .build();

        return count(request);
    }

    public Single<Long> count(ApartmentQueryRequest request) {
        return RxJava3Adapter.monoToSingle(repository.count(request));
    }

}
