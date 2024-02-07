package kyo.yaz.condominium.manager.persistence.repository.base;

import java.util.List;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BuildingCustomRepository {

  Mono<List<Building>> list(String filter, Pageable page);

  Mono<Building> updateAptCount(String id, long aptCount);

  @Cacheable("buildings-ids")
  Flux<String> getIds();
}
