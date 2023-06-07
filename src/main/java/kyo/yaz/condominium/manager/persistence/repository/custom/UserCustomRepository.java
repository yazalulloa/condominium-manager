package kyo.yaz.condominium.manager.persistence.repository.custom;

import java.util.List;
import kyo.yaz.condominium.manager.persistence.domain.request.UserQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserCustomRepository {


  Mono<List<User>> list(UserQueryRequest request);

  Flux<User> find(UserQueryRequest request);

  Mono<Long> count(UserQueryRequest request);
}
