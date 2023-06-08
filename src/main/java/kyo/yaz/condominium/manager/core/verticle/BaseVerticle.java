package kyo.yaz.condominium.manager.core.verticle;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import kyo.yaz.condominium.manager.core.vertx.VertxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseVerticle extends AbstractVerticle {

  protected final CompositeDisposable compositeDisposable = new CompositeDisposable();

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  public static String address(String addressIdentifier) {
    Objects.requireNonNull(addressIdentifier, "addressIdentifier MUST NOT BE NULL");
    final var trim = addressIdentifier.trim();
    if (trim.isEmpty()) {
      throw new RuntimeException("ADDRESS_MUST_NO_BE_EMPTY");
    }

    return addressIdentifier + "_" + UUID.randomUUID();
  }

  protected <T> void eventBusFunction(String address, Function<T, ?> function) {
    vertx.eventBus().<T>consumer(address, message -> {
      try {
        message.reply(function.apply(message.body()));
      } catch (Throwable e) {
        handle(e, message);
      }
    });
  }


  protected <T> void eventBusConsumer(String address, Function<T, Single<?>> function) {
    vertx.eventBus().<T>consumer(address, message -> event(message, function));
  }

  protected <T> void eventBusFuture(String address, Function<T, Future<?>> function) {
    vertx.eventBus().<T>consumer(address, message -> eventFuture(message, function));
  }

  protected <T> void eventFuture(Message<T> message, Function<T, Future<?>> function) {
    try {
      function.apply(message.body())
          .onComplete(ar -> {
            if (ar.failed()) {
              handle(ar.cause(), message);
            } else {
              message.reply(ar.result());
            }

          });
    } catch (Throwable e) {
      handle(e, message);
    }
  }

  protected void handle(Message<?> message, Throwable throwable) {
    if (message.replyAddress() == null) {
      handle(throwable, message);
    } else {

      if (throwable instanceof ReplyException) {
        message.reply(throwable);
      } else {
        message.reply(VertxUtil.replyException(throwable, getClass().toString()));
      }
    }
  }

  protected void handle(Throwable throwable, Message<?> message) {

    logger.error("ERROR", throwable);

  }

  protected <T> void eventBusConsumerEmptyBody(String address, Supplier<Single<?>> supplier) {
    vertx.eventBus().<T>consumer(address, message -> Single.defer(supplier::get).subscribe(singleObserver(message)));
  }

  protected <T> void eventBusSupplier(String address, Supplier<?> supplier) {
    vertx.eventBus().<T>consumer(address, message -> {
      try {
        message.reply(supplier.get());
      } catch (Exception e) {
        handle(e, message);
      }
    });
  }

  protected <T> void eventBusFutureSupplier(String address, Supplier<Future<?>> supplier) {
    vertx.eventBus().<T>consumer(address, message -> {
      try {
        supplier.get()
            .onComplete(ar -> {
              if (ar.failed()) {
                handle(ar.cause(), message);
              } else {
                message.reply(ar.result());
              }

            });
      } catch (Throwable e) {
        handle(e, message);
      }
    });
  }

  protected <T> void event(Message<T> message, Function<T, Single<?>> function) {
    Single.defer(() -> function.apply(message.body())).subscribe(singleObserver(message));
  }

  protected <T> SingleObserver<T> singleObserver(Message<?> message) {

    return new SingleObserver<T>() {
      @Override
      public void onSubscribe(@NonNull Disposable d) {

      }

      @Override
      public void onSuccess(@NonNull T t) {
        try {
          message.reply(t);
        } catch (Exception e) {
          handle(message, e);
        }
      }

      @Override
      public void onError(@NonNull Throwable e) {
        handle(message, e);

      }
    };
  }

  protected void subscribe(Completable... completables) {
    final var completable = Completable.mergeArrayDelayError(completables);
    subscribe(completable);
  }

  protected void subscribe(Completable completable) {
    final var disposable = completable
        //.subscribeOn(scheduler())
        .subscribe(() -> {
        }, throwable -> handle(throwable, null));

    compositeDisposable.add(disposable);
  }


  @Override
  public final void stop(Promise<Void> stopPromise) throws Exception {
    compositeDisposable.dispose();
    super.stop(stopPromise);
  }
}
