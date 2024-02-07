package kyo.yaz.condominium.manager.persistence.repository;

import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.repository.base.BuildingCustomRepository;
import kyo.yaz.condominium.manager.persistence.repository.base.ReactiveRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface BuildingRepository extends ReactiveMongoRepository<Building, String>, ReactiveRepository<Building>,
    BuildingCustomRepository {


}
