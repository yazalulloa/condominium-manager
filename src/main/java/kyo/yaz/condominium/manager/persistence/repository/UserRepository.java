package kyo.yaz.condominium.manager.persistence.repository;

import kyo.yaz.condominium.manager.persistence.entity.User;
import kyo.yaz.condominium.manager.persistence.repository.base.ReactiveRepository;
import kyo.yaz.condominium.manager.persistence.repository.custom.UserCustomRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String>, ReactiveRepository<User>
    , UserCustomRepository {

}
