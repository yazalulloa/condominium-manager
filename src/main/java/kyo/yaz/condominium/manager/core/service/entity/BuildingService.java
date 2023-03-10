package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.repository.BuildingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;


@Service
public class BuildingService {

    private final BuildingRepository repository;

    @Autowired
    public BuildingService(BuildingRepository repository) {
        this.repository = repository;
    }

    public Single<List<Building>> list(String filter) {
        return RxJava3Adapter.monoToSingle(repository.list(filter, Pageable.unpaged()));
    }

    public Single<Building> save(Building building) {
        return RxJava3Adapter.monoToSingle(repository.save(building));
    }

    public Completable delete(Building entity) {
        return RxJava3Adapter.monoToCompletable(repository.delete(entity));
    }

    public Single<LinkedHashSet<String>> buildingIds() {
        final var mono = repository.findAll()
                .map(Building::id)
                .sort(Comparator.naturalOrder())
                .collectList()
                .map(LinkedHashSet::new);

        return RxJava3Adapter.monoToSingle(mono);
    }

    public Maybe<Building> find(String id) {
        return RxJava3Adapter.monoToMaybe(repository.findById(id));
    }

    public Single<Building> get(String id) {
        return find(id)
                .switchIfEmpty(Single.error(new RuntimeException("Building not found")));
    }
}
