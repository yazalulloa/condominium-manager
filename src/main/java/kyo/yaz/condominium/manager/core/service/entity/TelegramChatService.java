package kyo.yaz.condominium.manager.core.service.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import kyo.yaz.condominium.manager.core.domain.FileResponse;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.service.paging.MongoServicePagingProcessorImpl;
import kyo.yaz.condominium.manager.core.service.paging.PagingJsonFile;
import kyo.yaz.condominium.manager.core.service.paging.WriteEntityToFile;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.domain.NotificationEvent;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.TelegramChatQueryRequest;
import kyo.yaz.condominium.manager.persistence.domain.request.TelegramChatQueryRequest.SortField;
import kyo.yaz.condominium.manager.persistence.entity.TelegramChat;
import kyo.yaz.condominium.manager.persistence.entity.TelegramChat.TelegramChatId;
import kyo.yaz.condominium.manager.persistence.repository.TelegramChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

@Service("TELEGRAM_CHATS")
@RequiredArgsConstructor
public class TelegramChatService implements MongoService<TelegramChat> {

  private final ObjectMapper objectMapper;
  private final TelegramChatRepository repository;


  public Maybe<TelegramChat> maybe(String userId, long chatId) {
    final var mono = repository.findById(new TelegramChatId(chatId, userId));
    return RxJava3Adapter.monoToMaybe(mono);
  }

  public Single<Optional<TelegramChat>> find(String userId, long chatId) {
    return maybe(userId, chatId).map(Optional::of)
        .defaultIfEmpty(Optional.empty());
  }

  public Single<Set<Long>> chatsByEvents(Set<NotificationEvent> events) {
    if (events.isEmpty()) {
      throw new IllegalArgumentException("Notifications must be not empty");
    }

    final var request = TelegramChatQueryRequest.builder()
        .notificationEvents(events)
        .build();

    return RxJava3Adapter.monoToSingle(repository.list(request))
        .flatMapObservable(Observable::fromIterable)
        .map(TelegramChat::chatId)
        .toList(HashSet::new);
  }

  public Completable delete(TelegramChat entity) {
    return RxJava3Adapter.monoToCompletable(repository.delete(entity));
  }

  public Single<Paging<TelegramChat>> paging(String filter, Set<NotificationEvent> value, int page, int pageSize) {

    final var sortings = new LinkedHashSet<Sorting<SortField>>();

    sortings.add(TelegramChatQueryRequest.sorting(SortField.CREATED_AT, Direction.DESC));

    final var request = TelegramChatQueryRequest.builder()
        .user(filter)
        .chat(filter)
        .notificationEvents(value)
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

  public Single<TelegramChat> save(TelegramChat entity) {
    return RxJava3Adapter.monoToSingle(repository.save(entity));
  }

  public Single<Boolean> saveOrUpdate(TelegramChat chat) {

    final var saveSingle = save(chat.updatedAt(DateUtil.nowZonedWithUTC()))
        .ignoreElement()
        .toSingleDefault(true);

    return maybe(chat.id().userId(), chat.id().chatId())
        .flatMapSingle(old -> {
          if (Objects.equals(old.notificationEvents(), chat.notificationEvents())) {
            return Single.just(false);
          }
          return saveSingle;

        })
        .switchIfEmpty(saveSingle);
  }

  public Single<List<TelegramChat>> list(int page, int pageSize) {
    final var mono = repository.findAllBy(PageRequest.of(page, pageSize))
        .collectList();

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Single<FileResponse> download() {

    final var writeEntityToFile = new WriteEntityToFile<>(objectMapper,
        new MongoServicePagingProcessorImpl<>(this, 20));

    return writeEntityToFile.downloadFile("telegram_chats.json.gz");

  }

  public Single<List<TelegramChat>> save(Iterable<TelegramChat> entities) {

    final var mono = repository.saveAll(entities)
        .collectList();

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Completable upload(String fileName) {
    return PagingJsonFile.pagingJsonFile(30, fileName, objectMapper, TelegramChat.class, c -> save(c).ignoreElement());
  }
}
