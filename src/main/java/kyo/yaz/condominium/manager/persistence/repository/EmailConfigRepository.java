package kyo.yaz.condominium.manager.persistence.repository;

import kyo.yaz.condominium.manager.persistence.entity.EmailConfig;
import kyo.yaz.condominium.manager.persistence.repository.base.EmailConfigCustomRepository;
import kyo.yaz.condominium.manager.persistence.repository.base.ReactiveRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface EmailConfigRepository extends ReactiveMongoRepository<EmailConfig, String>, ReactiveRepository<EmailConfig>, EmailConfigCustomRepository {
}
