package kyo.yaz.condominium.manager.core.service.entity;

import com.google.api.client.auth.oauth2.TokenResponseException;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.verticle.SendEmailVerticle;
import kyo.yaz.condominium.manager.core.vertx.VertxHandler;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.EmailConfigQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.EmailConfig;
import kyo.yaz.condominium.manager.persistence.repository.EmailConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class EmailConfigService {

    private final VertxHandler vertxHandler;
    private final EmailConfigRepository repository;

    @Autowired
    public EmailConfigService(VertxHandler vertxHandler, EmailConfigRepository repository) {
        this.vertxHandler = vertxHandler;
        this.repository = repository;
    }

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

    public Completable check(EmailConfig emailConfig) {
        return vertxHandler.get(SendEmailVerticle.CHECK_EMAIL_CONFIG, emailConfig)
                .map(o -> repository.updateCheck(emailConfig.id(), null))
                .onErrorReturn(throwable -> {

                    if (throwable instanceof ExecutionException && throwable.getCause() instanceof TokenResponseException) {
                        final var error = throwable.getCause().getMessage();
                        repository.updateCheck(emailConfig.id(), error);
                    }

                    log.info("CHECK_ERROR", throwable);

                    final var stackTrace = ExceptionUtils.getStackTrace(throwable);

                    return repository.updateCheck(emailConfig.id(), stackTrace);
                })
                .flatMapCompletable(RxJava3Adapter::monoToCompletable);
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
}
