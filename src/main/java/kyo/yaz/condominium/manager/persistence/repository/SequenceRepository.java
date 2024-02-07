package kyo.yaz.condominium.manager.persistence.repository;

import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import kyo.yaz.condominium.manager.persistence.repository.base.ReactiveRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface SequenceRepository extends ReactiveMongoRepository<Sequence, Sequence.Type>,
    ReactiveRepository<Sequence> {

}
