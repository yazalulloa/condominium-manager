package kyo.yaz.condominium.manager.persistence.repository.base;

import kyo.yaz.condominium.manager.persistence.domain.request.EmailConfigQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.EmailConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EmailConfigCustomRepository {

    Mono<List<EmailConfig>> list(EmailConfigQueryRequest request);

    Flux<EmailConfig> find(EmailConfigQueryRequest request);

    Mono<Long> count(EmailConfigQueryRequest request);

    Mono<EmailConfig> updateCheck(String id, String error);
}
