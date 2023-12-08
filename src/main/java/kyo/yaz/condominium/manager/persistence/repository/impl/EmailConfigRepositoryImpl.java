package kyo.yaz.condominium.manager.persistence.repository.impl;

import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.core.util.MongoDBUtil;
import kyo.yaz.condominium.manager.persistence.domain.request.EmailConfigQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.EmailConfig;
import kyo.yaz.condominium.manager.persistence.repository.base.EmailConfigCustomRepository;
import kyo.yaz.condominium.manager.persistence.util.QueryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class EmailConfigRepositoryImpl implements EmailConfigCustomRepository {

    @Autowired
    ReactiveMongoTemplate template;

    public Query query(EmailConfigQueryRequest request) {
        final var query = new Query();

        QueryUtil.addSortings(query, request.sortings());

        final List<Criteria> criteria = new ArrayList<>();

        Optional.ofNullable(request.filter())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .ifPresent(str -> {

                    MongoDBUtil.stringCriteria("id", str)
                            .ifPresent(criteria::add);

                    MongoDBUtil.stringCriteria("from", str)
                            .ifPresent(criteria::add);
                });

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().orOperator(criteria.toArray(new Criteria[0])));

        return query;
    }

    @Override
    public Mono<Long> count(EmailConfigQueryRequest request) {
        final var query = query(request);

        return template.count(query, EmailConfig.class);
    }

    @Override
    public Mono<EmailConfig> updateCheck(String id, String error) {
        final var query = new Query().addCriteria(Criteria.where("id").is(id));
        final var update = new Update().set("is_available", error == null).set("error", error).set("last_check_at", DateUtil.nowZonedWithUTC());
        final var options = new FindAndModifyOptions().returnNew(true).upsert(false);

        return template.findAndModify(query, update, options, EmailConfig.class);
    }

    @Override
    public Flux<EmailConfig> findByIdFrom() {
            final var query = new Query();
            query.fields().include("from");

            return find(query);
    }

    @Override
    public Mono<List<EmailConfig>> list(EmailConfigQueryRequest request) {

        return find(request)
                .collectList()
                .doOnSuccess(list -> {
                   // log.info("LIST: " + list.size());
                });
    }

    @Override
    public Flux<EmailConfig> find(EmailConfigQueryRequest request) {
        final var query = query(request);

        Optional.ofNullable(request.page())
                .ifPresent(query::with);

        //log.info("QUERY: " + query);

        return find(query);
    }

    private Flux<EmailConfig> find(Query query) {
        return template.find(query, EmailConfig.class);
    }

}
