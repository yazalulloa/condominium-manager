package kyo.yaz.condominium.manager.core.vertx;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import java.util.function.Consumer;

public class VertxHandlerImpl implements VertxHandler {

  private final Vertx vertx;

  public VertxHandlerImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  protected DeliveryOptions deliveryOptions() {
    return new DeliveryOptions().setSendTimeout(180000);
  }

  @Override
  public Vertx vertx() {
    return vertx;
  }

  @Override
  public <T> Single<Message<T>> request(String address, Object object, DeliveryOptions deliveryOptions) {
    return this.<Message<T>>single(ar -> vertx.eventBus().request(address, object, deliveryOptions, ar))
        .onErrorResumeNext(t -> Single.error(VertxUtil.removeReply(t)))
        ;
  }

  @Override
  public <T> Single<Message<T>> request(String address, Object object) {
    return request(address, object, deliveryOptions());
  }

  @Override
  public <T> Single<Message<T>> request(String address) {
    return request(address, new JsonObject());
  }

  @Override
  public <T> Single<T> get(String address, Object object, DeliveryOptions deliveryOptions) {

    return this.<T>request(address, object, deliveryOptions)
        .map(Message::body);
  }

  @Override
  public <T> Single<T> get(String address, Object object) {
    return get(address, object, deliveryOptions());
  }

  @Override
  public <T> Single<T> get(String address) {
    return get(address, new JsonObject());
  }

  @Override
  public <T> Single<T> single(Consumer<Handler<AsyncResult<T>>> consumer) {
    final var source = VertxUtil.singleOnSubscribe(consumer);
    return Single.create(source);
  }

  @Override
  public <T> Single<T> single(Future<T> future) {
    return single(future::onComplete);
  }

  @Override
  public <T> Maybe<T> maybe(Future<T> future) {
    return maybe(future::onComplete);
  }

  @Override
  public <T> Maybe<T> maybe(Consumer<Handler<AsyncResult<T>>> consumer) {
    final var source = VertxUtil.maybeOnSubscribe(consumer);
    return Maybe.create(source);
  }

  @Override
  public Completable completable(Consumer<Handler<AsyncResult<Void>>> consumer) {
    return VertxUtil.completable(consumer);
  }

  @Override
  public Completable completable(Future<Void> future) {
    return completable(future::onComplete);
  }

  @Override
  public Completable send(String address, Object object, DeliveryOptions deliveryOptions) {
    return completable(ar -> vertx.eventBus().sender(address, deliveryOptions).write(object, ar));
  }


  @Override
  public Completable send(String address, Object object) {
    return send(address, object, deliveryOptions());
  }

  @Override
  public Completable send(String address) {
    return send(address, new JsonObject());
  }

  @Override
  public Completable publish(String address, Object object, DeliveryOptions deliveryOptions) {
    return completable(ar -> vertx.eventBus().publisher(address, deliveryOptions).write(object, ar));
  }

  @Override
  public Completable publish(String address, Object object) {
    return publish(address, object, deliveryOptions());
  }

  @Override
  public Completable publish(String address) {
    return publish(address, new JsonObject());
  }

  @Override
  public Scheduler scheduler() {
    return VertxUtil.scheduler(vertx);
  }

  @Override
  public <K, V> Single<AsyncMap<K, V>> localAsyncMap(String name) {
    return single(ar -> vertx.sharedData().getLocalAsyncMap(name, ar));
  }

  @Override
  public <K, V> Single<AsyncMap<K, V>> asyncMap(String name) {
    return single(ar -> vertx.sharedData().getAsyncMap(name, ar));
  }

  @Override
  public Single<Counter> counter(String name) {
    return single(ar -> vertx.sharedData().getCounter(name, ar));
  }

  @Override
  public Single<Counter> localCounter(String name) {
    return single(ar -> vertx.sharedData().getLocalCounter(name, ar));
  }

  @Override
  public CompletableObserver subscriber(Promise<Void> promise) {
    return new CompletableObserver() {
      @Override
      public void onSubscribe(@NonNull Disposable d) {

      }

      @Override
      public void onComplete() {
        promise.complete();

      }

      @Override
      public void onError(@NonNull Throwable e) {
        promise.fail(e);
      }
    };
  }

  @Override
  public Flowable<Buffer> flowFileBuffer(String path, int readBufferSize) {
    return flowFileBuffer(path, readBufferSize, new OpenOptions());
  }

  @Override
  public Flowable<Buffer> flowFileBuffer(String path, int readBufferSize, OpenOptions openOptions) {

    return Single.fromCallable(() -> vertx.fileSystem().open(path, openOptions))
        .flatMap(this::single)
        .toFlowable()
        .flatMap(asyncFile -> {

          return Flowable.create(emitter -> asyncFile.setReadBufferSize(readBufferSize)
              .handler(emitter::onNext)
              .endHandler(v -> {
                asyncFile.close();
                emitter.onComplete();
              })
              .exceptionHandler(t -> {
                asyncFile.close();
                emitter.onError(t);
              }), BackpressureStrategy.BUFFER);
        });

  }
}

