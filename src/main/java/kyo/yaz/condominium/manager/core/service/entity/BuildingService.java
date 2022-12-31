package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.repository.BuildingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@Service
public class BuildingService {

    private final BuildingRepository repository;

    @Autowired
    public BuildingService(BuildingRepository repository) {
        this.repository = repository;
    }

    public Mono<List<Building>> list(String filter) {
        return repository.list(filter, Pageable.unpaged());
    }

    public Mono<Building> save(Building building) {
        return repository.save(building);
    }

    public Mono<Void> delete(Building entity) {
        return repository.delete(entity);
    }

    public Mono<Set<String>> buildingIds() {
        return repository.findAll()
                .map(Building::id)
                .sort(Comparator.naturalOrder())
                .collectList()
                .map(LinkedHashSet::new);
    }

    public Mono<Building> find(String id) {
        return repository.findById(id);
    }

    public Single<Building> get(String id) {
        return RxJava3Adapter.monoToMaybe(find(id))
                .switchIfEmpty(Single.error(new RuntimeException("Building not found")));
    }
}
