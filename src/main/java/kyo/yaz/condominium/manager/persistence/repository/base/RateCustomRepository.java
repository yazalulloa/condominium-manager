package kyo.yaz.condominium.manager.persistence.repository.base;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import kyo.yaz.condominium.manager.persistence.domain.request.RateQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RateCustomRepository {

  Mono<List<Rate>> list(RateQueryRequest request);

  Flux<Rate> find(RateQueryRequest request);

  Mono<Long> count(RateQueryRequest request);

  Stream<Rate> stream(RateQueryRequest request);

  Mono<Rate> updateHashesAndEtags(long id, Set<Long> hashes, Set<String> etags);
}
