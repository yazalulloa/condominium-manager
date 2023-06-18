package kyo.yaz.condominium.manager.core.service.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.TokenResponseException;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import kyo.yaz.condominium.manager.core.domain.FileResponse;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.service.NotificationService;
import kyo.yaz.condominium.manager.core.service.paging.MongoServicePagingProcessorImpl;
import kyo.yaz.condominium.manager.core.service.paging.PagingJsonFile;
import kyo.yaz.condominium.manager.core.service.paging.WriteEntityToFile;
import kyo.yaz.condominium.manager.core.verticle.SendEmailVerticle;
import kyo.yaz.condominium.manager.core.vertx.VertxHandler;
import kyo.yaz.condominium.manager.persistence.domain.NotificationEvent;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.EmailConfigQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.EmailConfig;
import kyo.yaz.condominium.manager.persistence.repository.EmailConfigRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

@Service("EMAIL_CONFIGS")
@Slf4j
@AllArgsConstructor
public class EmailConfigService implements MongoService<EmailConfig> {

  private final VertxHandler vertxHandler;
  private final ObjectMapper objectMapper;
  private final EmailConfigRepository repository;
  private final NotificationService notificationService;


  public Maybe<EmailConfig> find(String id) {
    return RxJava3Adapter.monoToMaybe(repository.findById(id));
  }

  public Single<EmailConfig> get(String id) {
    return find(id)
        .switchIfEmpty(Single.error(new RuntimeException("EmailConfig not found")));
  }

  public Single<Paging<EmailConfig>> paging(String filter, int page, int pageSize) {
    final var sortings = new LinkedHashSet<Sorting<EmailConfigQueryRequest.SortField>>();
    sortings.add(EmailConfigQueryRequest.sorting(EmailConfigQueryRequest.SortField.CREATED_AT, Sort.Direction.DESC));

    final var request = EmailConfigQueryRequest.builder()
        .filter(filter)
        .page(PageRequest.of(page, pageSize))
        .sortings(sortings)
        .build();

    return RxJava3Adapter.monoToSingle(paging(request));
  }

  private Mono<Paging<EmailConfig>> paging(EmailConfigQueryRequest request) {
    final var listMono = repository.list(request);
    final var totalCountMono = repository.count();
    final var queryCountMono = repository.count(request);

    return Mono.zip(totalCountMono, queryCountMono, listMono)
        .map(tuple -> new Paging<>(tuple.getT1(), tuple.getT2(), tuple.getT3()));
  }

  public Completable delete(EmailConfig entity) {
    return RxJava3Adapter.monoToCompletable(repository.delete(entity));
  }

  public Single<Boolean> save(EmailConfig entity) {

    final var saveSingle = RxJava3Adapter.monoToCompletable(repository.save(entity))
        .toSingleDefault(true);

    return RxJava3Adapter.monoToMaybe(repository.findById(entity.id()))
        .flatMapSingle(config -> {

          if (Objects.equals(entity.from(), config.from())
              && Objects.equals(entity.config(), config.config())
              && Objects.equals(entity.storedCredential(), config.storedCredential())
              && Objects.equals(entity.active(), config.active())
          ) {

            log.info("NOT UPDATING");
            return Single.just(false);
          }
          return saveSingle;

        })
        .switchIfEmpty(saveSingle);
  }

  public Single<Long> countAll() {
    return RxJava3Adapter.monoToSingle(repository.count());
  }

  public Completable clear() {
    return vertxHandler.get(SendEmailVerticle.CLEAR)
        .ignoreElement();
  }

  private Completable updateCheck(String id, String error) {
    return RxJava3Adapter.monoToCompletable(repository.updateCheck(id, error));
  }

  public Completable check(EmailConfig emailConfig) {
    return vertxHandler.get(SendEmailVerticle.CHECK_EMAIL_CONFIG, emailConfig)
        .map(o -> updateCheck(emailConfig.id(), null))
        .onErrorReturn(throwable -> {

          final var isInvalidToken =
              throwable instanceof ExecutionException && throwable.getCause() instanceof TokenResponseException;

          if (isInvalidToken) {
            log.info("CHECK_ERROR\n{}", throwable.getMessage());
          } else {
            log.info("CHECK_ERROR", throwable);
          }

          final var stackTrace =
              isInvalidToken
                  ? throwable.getMessage() :
                  ExceptionUtils.getStackTrace(throwable);

          final var msg = "Error Email Config %s %s %s".formatted(emailConfig.id(), emailConfig.from(),
              throwable.getMessage());
          final var notification =
              emailConfig.isAvailable() ? notificationService.send(msg, NotificationEvent.CONFIG_EMAIL_FAILED_CHECK)
                  : Completable.complete();

          return updateCheck(emailConfig.id(), stackTrace)
              .andThen(notification);
        })
        .flatMapCompletable(c -> c);
  }

  public Completable checkAll() {
    return RxJava3Adapter.fluxToFlowable(repository.findAll())
        .filter(EmailConfig::isAvailable)
        .map(this::check)
        .toList()
        .flatMapCompletable(Completable::concat);
  }

  public Single<List<EmailConfig>> listForComboBox() {
    final var mono = repository.findByIdFrom().collectList();
    return RxJava3Adapter.monoToSingle(mono);
  }

  public Single<List<EmailConfig>> list(int page, int pageSize) {
    final var mono = repository.findAllBy(PageRequest.of(page, pageSize))
        .collectList();

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Single<FileResponse> download() {

    final var writeEntityToFile = new WriteEntityToFile<>(objectMapper,
        new MongoServicePagingProcessorImpl<>(this, 20));

    return writeEntityToFile.downloadFile("email_configs.json.gz");

  }

  public Single<List<EmailConfig>> save(Iterable<EmailConfig> entities) {

    final var mono = repository.saveAll(entities)
        .collectList();

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Completable upload(String fileName) {
    return PagingJsonFile.pagingJsonFile(30, fileName, objectMapper, EmailConfig.class, c -> save(c).ignoreElement());
  }
}
