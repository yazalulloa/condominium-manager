package kyo.yaz.condominium.manager.core.service;

import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.ApartmentQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.repository.ApartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class ApartmentService {

    private final ApartmentRepository repository;

    @Autowired
    public ApartmentService(ApartmentRepository repository) {
        this.repository = repository;
    }

    public Mono<Apartment> read(String buildingId, String aptNumber) {
        return repository.findById(Apartment.ApartmentId.of(buildingId, aptNumber));
    }

    public Mono<Paging<Apartment>> paging(Set<String> buildings, String filter, int page, int pageSize) {

        final var sortings = new LinkedHashSet<Sorting<ApartmentQueryRequest.SortField>>();
        sortings.add(ApartmentQueryRequest.sorting(ApartmentQueryRequest.SortField.BUILDING_ID, Sort.Direction.ASC));
        sortings.add(ApartmentQueryRequest.sorting(ApartmentQueryRequest.SortField.NUMBER, Sort.Direction.ASC));

        final var request = ApartmentQueryRequest.builder()
                .buildings(buildings)
                .apartment(filter)
                .page(PageRequest.of(page, pageSize))
                .sortings(sortings)
                .build();

        final var listMono = repository.list(request);
        final var totalCountMono = repository.count();
        final var queryCountMono = repository.count(request);

        return Mono.zip(totalCountMono, queryCountMono, listMono)
                .map(tuple -> new Paging<>(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    public Mono<List<String>> aptNumbers(String buildingId) {
        final var sortings = new LinkedHashSet<Sorting<ApartmentQueryRequest.SortField>>();
        sortings.add(ApartmentQueryRequest.sorting(ApartmentQueryRequest.SortField.NUMBER, Sort.Direction.ASC));
        final var request = ApartmentQueryRequest.builder()
                .buildings(Collections.singleton(buildingId))
                .sortings(sortings)
                .build();

        return repository.find(request)
                .map(Apartment::apartmentId)
                .map(Apartment.ApartmentId::number)
                .collectList();
    }

    public Mono<Void> delete(Apartment entity) {
        return repository.delete(entity);
    }

    public Mono<Apartment> save(Apartment apartment) {
        return repository.save(apartment);
    }

    public Mono<Long> countAll() {
        return repository.count();
    }

}
