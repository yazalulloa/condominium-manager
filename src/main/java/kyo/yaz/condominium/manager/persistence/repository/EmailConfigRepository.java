package kyo.yaz.condominium.manager.persistence.repository;

import kyo.yaz.condominium.manager.persistence.entity.EmailConfig;
import kyo.yaz.condominium.manager.persistence.repository.base.EmailConfigCustomRepository;
import kyo.yaz.condominium.manager.persistence.repository.base.ReactiveRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface EmailConfigRepository extends ReactiveMongoRepository<EmailConfig, String>, ReactiveRepository<EmailConfig>, EmailConfigCustomRepository {



}
