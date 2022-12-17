package kyo.yaz.condominium.manager.persistence.repository.base;

import kyo.yaz.condominium.manager.persistence.domain.request.ReceiptQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReceiptCustomRepository {

    Mono<List<Receipt>> list(ReceiptQueryRequest request);

    Flux<Receipt> find(ReceiptQueryRequest request);
}
