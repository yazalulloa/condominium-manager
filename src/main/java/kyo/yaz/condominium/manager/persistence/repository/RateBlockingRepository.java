package kyo.yaz.condominium.manager.persistence.repository;

import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.persistence.repository.base.RateBlockingCustomRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RateBlockingRepository extends MongoRepository<Rate, Long>, RateBlockingCustomRepository {
}
