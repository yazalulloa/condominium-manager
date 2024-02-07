package kyo.yaz.condominium.manager.persistence.repository.base;

import java.util.List;
import kyo.yaz.condominium.manager.persistence.domain.request.EmailConfigQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.EmailConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmailConfigCustomRepository {

  Mono<List<EmailConfig>> list(EmailConfigQueryRequest request);

  Flux<EmailConfig> find(EmailConfigQueryRequest request);

  Mono<Long> count(EmailConfigQueryRequest request);

  Mono<EmailConfig> updateCheck(String id, String error);

  Flux<EmailConfig> findByIdFrom();
}
