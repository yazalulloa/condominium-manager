package kyo.yaz.condominium.manager.persistence.repository.custom;

import java.util.List;
import kyo.yaz.condominium.manager.persistence.domain.request.TelegramChatQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.TelegramChat;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TelegramChatCustomRepository {


  Mono<List<TelegramChat>> list(TelegramChatQueryRequest request);

  Flux<TelegramChat> find(TelegramChatQueryRequest request);

  Mono<Long> count(TelegramChatQueryRequest request);
}
