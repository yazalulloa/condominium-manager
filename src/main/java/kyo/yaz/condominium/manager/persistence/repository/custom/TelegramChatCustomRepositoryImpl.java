package kyo.yaz.condominium.manager.persistence.repository.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import kyo.yaz.condominium.manager.core.util.MongoDBUtil;
import kyo.yaz.condominium.manager.persistence.domain.request.TelegramChatQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.TelegramChat;
import kyo.yaz.condominium.manager.persistence.util.QueryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TelegramChatCustomRepositoryImpl implements TelegramChatCustomRepository {

  @Autowired
  ReactiveMongoTemplate template;


  public Query queryRequest(TelegramChatQueryRequest request) {
    final var query = new Query();

    QueryUtil.addSortings(query, request.sortings());

    final List<Criteria> criteriaList = new ArrayList<>();

    final var notificationEvents = Optional.ofNullable(request.notificationEvents())
        .filter(s -> !s.isEmpty())
        .stream()
        .flatMap(Collection::stream)
        .map(Enum::name)
        .map(str -> MongoDBUtil.stringCriteria("notification_events", str))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();

    if (!notificationEvents.isEmpty()) {
      criteriaList.add(new Criteria().orOperator(notificationEvents));
    }

    MongoDBUtil.regexCriteria(request.user(), "user.name", "user.given_name", "user.id", "user.email")
        .ifPresent(criteriaList::add);

    MongoDBUtil.regexCriteria(request.chat(), "first_name", "last_name", "_id", "username")
        .ifPresent(criteriaList::add);

    if (!criteriaList.isEmpty()) {
      query.addCriteria(new Criteria().andOperator(criteriaList));
    }

    return query;
  }

  @Override
  public Mono<List<TelegramChat>> list(TelegramChatQueryRequest request) {

    return find(request)
        .collectList();
  }

  @Override
  public Flux<TelegramChat> find(TelegramChatQueryRequest request) {
    final var query = queryRequest(request);

    Optional.ofNullable(request.page())
        .ifPresent(query::with);

    return template.find(query, TelegramChat.class);
  }

  @Override
  public Mono<Long> count(TelegramChatQueryRequest request) {
    final var query = queryRequest(request);

    return template.count(query, TelegramChat.class);
  }
}
