package kyo.yaz.condominium.manager.core.vertx;

import io.reactivex.rxjava3.core.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.rxjava3.RxHelper;

import java.util.function.Consumer;

public class VertxUtil {
    public static Scheduler scheduler(Vertx vertx) {
        return RxHelper.scheduler(vertx);
    }

    public static <T> Handler<AsyncResult<T>> actionListener(MaybeEmitter<T> emitter) {
        return result -> {
            if (result.succeeded()) {
                if (result.result() == null) {
                    emitter.onComplete();
                } else {
                    emitter.onSuccess(result.result());
                }
            } else {
                emitter.onError(result.cause());
            }
        };
    }

    public static <T> Handler<AsyncResult<T>> actionListener(SingleEmitter<T> emitter) {
        return result -> {
            if (result.succeeded()) {
                emitter.onSuccess(result.result());
            } else {
                emitter.onError(result.cause());
            }
        };
    }

    public static Handler<AsyncResult<Void>> actionListener(CompletableEmitter emitter) {
        return result -> {
            if (result.succeeded()) {
                emitter.onComplete();
            } else {
                emitter.onError(result.cause());
            }
        };
    }

    public static <T> SingleOnSubscribe<T> singleOnSubscribe(Consumer<Handler<AsyncResult<T>>> consumer) {
        return emitter -> {
            final Handler<AsyncResult<T>> actionListener = actionListener(emitter);
            consumer.accept(actionListener);
        };
    }

    public static <T> MaybeOnSubscribe<T> maybeOnSubscribe(Consumer<Handler<AsyncResult<T>>> consumer) {
        return emitter -> {
            final Handler<AsyncResult<T>> actionListener = actionListener(emitter);
            consumer.accept(actionListener);
        };
    }

    public static CompletableOnSubscribe completableOnSubscribe(Consumer<Handler<AsyncResult<Void>>> consumer) {
        return emitter -> {
            final var actionListener = actionListener(emitter);
            consumer.accept(actionListener);
        };
    }

    public static Completable completable(Consumer<Handler<AsyncResult<Void>>> consumer) {
        final var source = completableOnSubscribe(consumer);
        return Completable.create(source);
    }
}
