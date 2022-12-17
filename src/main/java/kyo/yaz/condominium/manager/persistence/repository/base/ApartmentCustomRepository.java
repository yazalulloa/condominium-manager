package kyo.yaz.condominium.manager.persistence.repository.base;

import kyo.yaz.condominium.manager.persistence.domain.request.ApartmentQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApartmentCustomRepository {

    Mono<List<Apartment>> list(ApartmentQueryRequest request);

    Flux<Apartment> find(ApartmentQueryRequest request);

    Mono<Long> count(ApartmentQueryRequest request);
}
