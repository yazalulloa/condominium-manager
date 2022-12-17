package kyo.yaz.condominium.manager.persistence.repository;

import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.repository.base.ReactiveRepository;
import kyo.yaz.condominium.manager.persistence.repository.base.ReceiptCustomRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ReceiptRepository extends ReactiveMongoRepository<Receipt, Long>, ReactiveRepository<Receipt>, ReceiptCustomRepository {
}
