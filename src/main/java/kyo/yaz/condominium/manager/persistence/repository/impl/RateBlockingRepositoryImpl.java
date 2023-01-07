package kyo.yaz.condominium.manager.persistence.repository.impl;

import kyo.yaz.condominium.manager.persistence.domain.request.RateQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.persistence.repository.base.RateBlockingCustomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.util.StreamUtils;

import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class RateBlockingRepositoryImpl implements RateBlockingCustomRepository {

    @Autowired
    MongoTemplate template;


    @Override
    public Stream<Rate> stream(RateQueryRequest request) {

        final var query = RateRepositoryImpl.query(request);

        Optional.ofNullable(request.page())
                .ifPresent(query::with);

        log.info("QUERY: " + query);

        return StreamUtils.createStreamFromIterator(template.stream(query, Rate.class));
    }
}
