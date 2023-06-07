package kyo.yaz.condominium.manager.persistence.repository.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kyo.yaz.condominium.manager.core.util.MongoDBUtil;
import kyo.yaz.condominium.manager.persistence.domain.request.UserQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.User;
import kyo.yaz.condominium.manager.persistence.util.QueryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class UserCustomRepositoryImpl implements UserCustomRepository {

  @Autowired
  ReactiveMongoTemplate template;


  public Query queryRequest(UserQueryRequest request) {
    final var query = new Query();

    QueryUtil.addSortings(query, request.sortings());

    final List<Criteria> criteriaList = new ArrayList<>();

    MongoDBUtil.regexCriteria(request.user(), "name", "given_name", "_id", "email")
        .ifPresent(criteriaList::add);

    if (!criteriaList.isEmpty()) {
      query.addCriteria(new Criteria().andOperator(criteriaList));
    }

    return query;
  }

  @Override
  public Mono<List<User>> list(UserQueryRequest request) {

    return find(request)
        .collectList();
  }

  @Override
  public Flux<User> find(UserQueryRequest request) {
    final var query = queryRequest(request);

    Optional.ofNullable(request.page())
        .ifPresent(query::with);

    return template.find(query, User.class);
  }

  @Override
  public Mono<Long> count(UserQueryRequest request) {
    final var query = queryRequest(request);

    return template.count(query, User.class);
  }
}
