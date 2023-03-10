package kyo.yaz.condominium.manager.persistence.repository.base;

import kyo.yaz.condominium.manager.persistence.entity.Building;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BuildingCustomRepository {

    Mono<List<Building>> list(String filter, Pageable page);
}
