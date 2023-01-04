package kyo.yaz.condominium.manager.core.vertx;

import io.reactivex.rxjava3.core.*;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;

import java.util.function.Consumer;

public interface VertxHandler {

    Vertx vertx();

    <T> Single<Message<T>> request(String address, Object object, DeliveryOptions deliveryOptions);

    <T> Single<Message<T>> request(String address, Object object);

    <T> Single<Message<T>> request(String address);

    <T> Single<T> get(String address, Object object, DeliveryOptions deliveryOptions);

    <T> Single<T> get(String address, Object object);

    <T> Single<T> get(String address);

    <T> Single<T> single(Consumer<Handler<AsyncResult<T>>> consumer);

    <T> Single<T> single(Future<T> future);

    <T> Maybe<T> maybe(Future<T> future);

    <T> Maybe<T> maybe(Consumer<Handler<AsyncResult<T>>> consumer);

    Completable completable(Consumer<Handler<AsyncResult<Void>>> consumer);

    Completable completable(Future<Void> future);

    Completable send(String address, Object object, DeliveryOptions deliveryOptions);

    Completable send(String address, Object object);

    Completable send(String address);

    Completable publish(String address, Object object, DeliveryOptions deliveryOptions);

    Completable publish(String address, Object object);

    Completable publish(String address);

    Scheduler scheduler();

    <K, V> Single<AsyncMap<K, V>> localAsyncMap(String name);

    <K, V> Single<AsyncMap<K, V>> asyncMap(String name);

    Single<Counter> counter(String name);

    Single<Counter> localCounter(String name);

    CompletableObserver subscriber(Promise<Void> promise);

    Flowable<Buffer> flowFileBuffer(String path, int readBufferSize);

    Flowable<Buffer> flowFileBuffer(String path, int readBufferSize, OpenOptions openOptions);
}
