package kyo.yaz.condominium.manager.persistence.repository.impl;

import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.repository.base.BuildingCustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class BuildingRepositoryImpl implements BuildingCustomRepository {
    @Autowired
    ReactiveMongoTemplate template;

    @Override
    public Mono<List<Building>> list(String filter, Pageable page) {
        final var query = new Query().with(page);
//     query.fields().include("id").include("name");
        final List<Criteria> criteria = new ArrayList<>();
        Optional.ofNullable(filter)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .ifPresent(str -> {

                    criteria.add(Criteria.where("id").is(str));
                    criteria.add(Criteria.where("name").is(str));
                    criteria.add(Criteria.where("rif").is(str));
                });


        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().orOperator(criteria.toArray(new Criteria[0])));
        return template.find(query, Building.class)
                .collectList();
    }
}
