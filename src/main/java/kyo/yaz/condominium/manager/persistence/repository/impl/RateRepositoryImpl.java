package kyo.yaz.condominium.manager.persistence.repository.impl;

import kyo.yaz.condominium.manager.persistence.domain.request.RateQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.persistence.repository.base.RateCustomRepository;
import kyo.yaz.condominium.manager.persistence.util.QueryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class RateRepositoryImpl implements RateCustomRepository {

    @Autowired
    ReactiveMongoTemplate template;

    public static Query query(RateQueryRequest request) {
        final var query = new Query();

        QueryUtil.addSortings(query, request.sortings());

        final List<Criteria> criteriaList = new ArrayList<>();

        Optional.ofNullable(request.fromCurrency())
                .filter(s -> !s.isEmpty())
                .map(set -> {

                    final var list = set.stream().map(Enum::name)
                            .map(str -> Criteria.where("from_currency").is(str))
                            .collect(Collectors.toList());

                    return new Criteria().orOperator(list);

                })
                .ifPresent(criteriaList::add);


        Optional.ofNullable(request.toCurrency())
                .filter(s -> !s.isEmpty())
                .map(set -> {

                    final var list = set.stream().map(Enum::name)
                            .map(str -> Criteria.where("to_currency").is(str))
                            .collect(Collectors.toList());

                    return new Criteria().orOperator(list);

                })
                .ifPresent(criteriaList::add);


        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList));
        }

        return query;
    }

    @Override
    public Mono<List<Rate>> list(RateQueryRequest request) {

        return find(request)
                .collectList()
                .doOnSuccess(list -> {
                    log.info("LIST: " + list.size());
                });
    }

    @Override
    public Flux<Rate> find(RateQueryRequest request) {
        final var query = query(request);

        Optional.ofNullable(request.page())
                .ifPresent(query::with);

        log.info("QUERY: " + query);

        return template.find(query, Rate.class);
    }

    @Override
    public Mono<Long> count(RateQueryRequest request) {
        final var query = query(request);

        return template.count(query, Rate.class);
    }
}
