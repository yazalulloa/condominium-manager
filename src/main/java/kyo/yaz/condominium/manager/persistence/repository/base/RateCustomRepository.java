package kyo.yaz.condominium.manager.persistence.repository.base;

import kyo.yaz.condominium.manager.persistence.domain.request.RateQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RateCustomRepository {

    Mono<List<Rate>> list(RateQueryRequest request);

    Flux<Rate> find(RateQueryRequest request);

    Mono<Long> count(RateQueryRequest request);
}
