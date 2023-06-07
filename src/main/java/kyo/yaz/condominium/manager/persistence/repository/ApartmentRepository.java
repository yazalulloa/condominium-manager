package kyo.yaz.condominium.manager.persistence.repository;

import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.repository.base.ApartmentCustomRepository;
import kyo.yaz.condominium.manager.persistence.repository.base.ReactiveRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ApartmentRepository extends ReactiveMongoRepository<Apartment, Apartment.ApartmentId>, ReactiveRepository<Apartment>,
    ApartmentCustomRepository {
}
