package kyo.yaz.condominium.manager.core.verticle;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.util.Objects;
import kyo.yaz.condominium.manager.core.service.entity.UserService;
import kyo.yaz.condominium.manager.persistence.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ProcessLoggedUserVerticle extends BaseVerticle {

  public static final String ADDRESS = "process-user";


  private final UserService userService;

  @Autowired
  public ProcessLoggedUserVerticle(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void start() throws Exception {
    vertx.eventBus().<User>consumer(ADDRESS, message -> {

      final var user = message.body();
      //log.info("USER {}", user.toString());

      final var userSavedSingle = Single.fromCallable(() ->
              userService.save(user)
                  .ignoreElement()
          //    .doOnComplete(() -> log.info("USER_SAVED"))
      );

      final var completable = userService.maybe(user.id())
          .map(old -> {

            if (!Objects.equals(old.lastAccessTokenHash(), user.lastAccessTokenHash())) {

              return userService.save(user.toBuilder()
                      .createdAt(old.createdAt())
                      .build())
                  .ignoreElement()
                  //.doOnComplete(() -> log.info("USER_UPDATED"))
                  ;
            }

            //  log.info("USER_NOT_UPDATED");

            return Completable.complete();

          })
          .switchIfEmpty(userSavedSingle)
          .flatMapCompletable(c -> c);

      subscribe(completable);
    });
  }
}
