package kyo.yaz.condominium.manager.persistence.repository.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import kyo.yaz.condominium.manager.core.util.MongoDBUtil;
import kyo.yaz.condominium.manager.persistence.domain.request.ReceiptQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.repository.base.ReceiptCustomRepository;
import kyo.yaz.condominium.manager.persistence.util.QueryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class ReceiptRepositoryImpl implements ReceiptCustomRepository {

  @Autowired
  ReactiveMongoTemplate template;

  @Override
  public Mono<List<Receipt>> list(ReceiptQueryRequest request) {

    return find(request)
        .collectList()
        .doOnSuccess(list -> {
          //log.info("LIST: " + list.size());
        });
  }

  @Override
  public Flux<Receipt> find(ReceiptQueryRequest request) {
    final var query = query(request);

    //log.info("QUERY: " + query);
    return template.find(query, Receipt.class);
  }

  @Override
  public Mono<Long> count(ReceiptQueryRequest request) {
    final var query = query(request);

    return template.count(query, Receipt.class);
  }

  public Query query(ReceiptQueryRequest request) {
    final var query = new Query();

    Optional.ofNullable(request.page())
        .ifPresent(query::with);

    QueryUtil.addSortings(query, request.sortings());

    final List<Criteria> criteriaList = new ArrayList<>();

    MongoDBUtil.regexCriteria(request.filter(), "expenses.description", "extra_charges.description")
        .ifPresent(criteriaList::add);

    final var months = Optional.ofNullable(request.months())
        .filter(s -> !s.isEmpty())
        .stream()
        .flatMap(Collection::stream)
        .map(Enum::name)
        .map(str -> MongoDBUtil.stringCriteria("month", str))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();

    if (!months.isEmpty()) {
      criteriaList.add(new Criteria().orOperator(months));
    }

    final var buildings = Optional.ofNullable(request.buildings())
        .filter(s -> !s.isEmpty())
        .stream()
        .flatMap(Collection::stream)
        .map(str -> MongoDBUtil.stringCriteria("building_id", str))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();

    if (!buildings.isEmpty()) {
      criteriaList.add(new Criteria().orOperator(buildings));
    }

    if (!criteriaList.isEmpty()) {
      query.addCriteria(new Criteria().andOperator(criteriaList));
    }

    return query;
  }
}
