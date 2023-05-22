package kyo.yaz.condominium.manager.persistence.repository.impl;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ReceiptRepositoryImpl implements ReceiptCustomRepository {
    @Autowired
    ReactiveMongoTemplate template;

    @Override
    public Mono<List<Receipt>> list(ReceiptQueryRequest request) {

        return find(request)
                .collectList()
                .doOnSuccess(list -> {
                    log.info("LIST: " + list.size());
                });
    }

    @Override
    public Flux<Receipt> find(ReceiptQueryRequest request) {
        final var query = query(request);

        log.info("QUERY: " + query);
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

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList));
        }

        return query;
    }
}
