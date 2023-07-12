package kyo.yaz.condominium.manager.persistence.repository;

import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.persistence.repository.base.RateCustomRepository;
import kyo.yaz.condominium.manager.persistence.repository.base.ReactiveRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RateRepository extends ReactiveMongoRepository<Rate, Long>, ReactiveRepository<Rate>,
        RateCustomRepository {


}
