package kyo.yaz.condominium.manager.core.vertx;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import java.util.ArrayList;
import java.util.List;
import kyo.yaz.condominium.manager.core.util.RxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Rx3Verticle extends AbstractVerticle {

  protected final CompositeDisposable compositeDisposable = new CompositeDisposable();
  protected final List<Runnable> closeList = new ArrayList<>();

  protected final Logger logger = LoggerFactory.getLogger(getClass());


  protected Scheduler scheduler() {
    return VertxUtil.scheduler(vertx);
  }

  protected VertxHandler vertxHandler() {
    return new VertxHandlerImpl(vertx);
  }


  protected <T> SingleObserver<T> singleObserver(Message<?> message) {

    return RxUtil.singleObserver(compositeDisposable::add, obj -> {
      try {
        message.reply(obj);
      } catch (Exception e) {
        handle(message, e);
      }
    }, throwable -> handle(message, throwable));

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

  protected void subscribe(Completable... completables) {
    final var completable = Completable.mergeArrayDelayError(completables);
    subscribe(completable);
  }

  protected void subscribe(Completable completable) {
    final var disposable = completable.subscribeOn(scheduler())
        .subscribe(() -> {
        }, throwable -> handle(throwable, null));

    compositeDisposable.add(disposable);
  }

  @Override
  public void stop(Promise<Void> promise) throws Exception {

    closeList.forEach(runnable -> {
      try {
        runnable.run();
      } catch (Exception e) {
        promise.fail(e);
      }

    });
    compositeDisposable.clear();
    stop();
    promise.complete();
  }


}
