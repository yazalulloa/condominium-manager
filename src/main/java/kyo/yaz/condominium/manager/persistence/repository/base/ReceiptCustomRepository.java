package kyo.yaz.condominium.manager.persistence.repository.base;

import java.util.List;
import kyo.yaz.condominium.manager.persistence.domain.request.ReceiptQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReceiptCustomRepository {

  Mono<List<Receipt>> list(ReceiptQueryRequest request);

  Flux<Receipt> find(ReceiptQueryRequest request);

  Mono<Long> count(ReceiptQueryRequest request);
}
