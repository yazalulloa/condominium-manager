package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.util.LinkedHashSet;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.TelegramChatQueryRequest;
import kyo.yaz.condominium.manager.persistence.domain.request.TelegramChatQueryRequest.SortField;
import kyo.yaz.condominium.manager.persistence.entity.TelegramChat;
import kyo.yaz.condominium.manager.persistence.repository.TelegramChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

@Service
public class TelegramChatService {

  private final TelegramChatRepository repository;

  @Autowired
  public TelegramChatService(TelegramChatRepository repository) {
    this.repository = repository;
  }

  public Maybe<TelegramChat> find(String id) {
    final var mono = repository.findById(id);
    return RxJava3Adapter.monoToMaybe(mono);
  }

  public Completable delete(TelegramChat entity) {
    return RxJava3Adapter.monoToCompletable(repository.delete(entity));
  }

  public Single<Paging<TelegramChat>> paging(String filter, int page, int pageSize) {

    final var sortings = new LinkedHashSet<Sorting<SortField>>();

    sortings.add(TelegramChatQueryRequest.sorting(SortField.CREATED_AT, Direction.DESC));

    final var request = TelegramChatQueryRequest.builder()
        .user(filter)
        .chat(filter)
        .page(PageRequest.of(page, pageSize))
        .sortings(sortings)
        .build();

    return RxJava3Adapter.monoToSingle(paging(request));
  }

  private Mono<Paging<TelegramChat>> paging(TelegramChatQueryRequest request) {
    final var listMono = repository.list(request);
    final var totalCountMono = repository.count();
    final var queryCountMono = repository.count(request);

    return Mono.zip(totalCountMono, queryCountMono, listMono)
        .map(tuple -> new Paging<>(tuple.getT1(), tuple.getT2(), tuple.getT3()));
  }
}
