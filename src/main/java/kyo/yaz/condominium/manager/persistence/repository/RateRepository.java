package kyo.yaz.condominium.manager.persistence.repository;

import java.util.UUID;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.persistence.repository.base.RateCustomRepository;
import kyo.yaz.condominium.manager.persistence.repository.base.ReactiveRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface RateRepository extends ReactiveMongoRepository<Rate, UUID>, ReactiveRepository<Rate>,
    RateCustomRepository {


}
