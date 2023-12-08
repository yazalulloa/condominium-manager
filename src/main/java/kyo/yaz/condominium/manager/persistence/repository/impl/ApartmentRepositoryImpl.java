package kyo.yaz.condominium.manager.persistence.repository.impl;

import kyo.yaz.condominium.manager.core.util.MongoDBUtil;
import kyo.yaz.condominium.manager.core.util.StringUtil;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.ApartmentQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.repository.base.ApartmentCustomRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ApartmentRepositoryImpl implements ApartmentCustomRepository {

    @Autowired
    ReactiveMongoTemplate template;

    public Query query(ApartmentQueryRequest request) {
        final var query = new Query();


        QueryUtil.addSortings(query, request.sortings());

        final List<Criteria> criteriaList = new ArrayList<>();

        Optional.ofNullable(request.buildings())
                .filter(s -> !s.isEmpty())
                .map(set -> {

                    final var list = set.stream().map(str -> {
                                return MongoDBUtil.stringCriteria("_id.building_id", str);
                            })
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());

                    return new Criteria().orOperator(list);

                })
                .ifPresent(criteriaList::add);

        MongoDBUtil.stringCriteria("_id.number", request.number())
                .ifPresent(criteriaList::add);

        MongoDBUtil.stringCriteria("name", request.name())
                .ifPresent(criteriaList::add);

        MongoDBUtil.stringCriteria("id_doc", request.idDoc())
                .ifPresent(criteriaList::add);

        MongoDBUtil.stringCriteria("emails", request.email())
                .ifPresent(criteriaList::add);

        final var list = new ArrayList<Criteria>();
        StringUtil.trimFilter(request.apartment())
                .ifPresent(str -> {

                    //final var list = new ArrayList<Criteria>();

                    if (request.number() == null) {
                        list.add(Criteria.where("_id.number").regex(".*" + str + ".*", "i"));
                    }

                    if (request.name() == null) {
                        list.add(Criteria.where("name").regex(".*" + str + ".*", "i"));
                    }

                    if (request.idDoc() == null) {
                        list.add(Criteria.where("id_doc").regex(".*" + str + ".*", "i"));
                    }

                    if (request.email() == null) {
                        list.add(Criteria.where("emails").regex(".*" + str + ".*", "i"));
                    }

                    if (!list.isEmpty()) {
                        criteriaList.add(new Criteria().orOperator(list));
                    }

                });

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList));
        }

        return query;
    }

    @Override
    public Mono<List<Apartment>> list(ApartmentQueryRequest request) {

        return find(request)
                .collectList()
                .doOnSuccess(list -> {
                   // log.info("LIST: " + list.size());
                });
    }

    @Override
    public Flux<Apartment> find(ApartmentQueryRequest request) {
        final var query = query(request);

        Optional.ofNullable(request.page())
                .ifPresent(query::with);

       // log.info("QUERY: " + query);

        return template.find(query, Apartment.class);
    }

    @Override
    public Mono<Long> count(ApartmentQueryRequest request) {
        final var query = query(request);

        return template.count(query, Apartment.class);
    }

    @Override
    public Flux<Apartment> getAptNumberName(String buildingId) {
        final var query = new Query().addCriteria(Criteria.where("_id.building_id").is(buildingId));
        query.fields().include("name");
        return find(query);
    }

    private Flux<Apartment> find(Query query) {
        return template.find(query, Apartment.class);
    }
}
