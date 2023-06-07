package kyo.yaz.condominium.manager.persistence.repository.base;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

public interface ReactiveRepository<T> {

    Flux<T> findAllBy(Pageable pageable);
}
