package kyo.yaz.condominium.manager.core.service.entity;

import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.repository.BuildingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashSet;
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
                .sort()
                .collectList()
                .map(HashSet::new);
    }

    public Mono<Building> find(String id) {
        return repository.findById(id);
    }
}
