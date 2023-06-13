package kyo.yaz.condominium.manager.core.util;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RxUtil {

  public static CompletableObserver completableObserver(@NonNull Consumer<Disposable> disposableConsumer,
      @NonNull Action onComplete, @NonNull Consumer<? super Throwable> onError) {
    return new CompletableObserver() {
      @Override
      public void onSubscribe(@NonNull Disposable d) {
        try {
          disposableConsumer.accept(d);
        } catch (Throwable e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void onComplete() {
        try {
          onComplete.run();
        } catch (Throwable e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void onError(@NonNull Throwable throwable) {
        try {
          onError.accept(throwable);
        } catch (Throwable e) {
          throw new RuntimeException(e);

        }
      }
    };
  }

  public static <T> SingleObserver<T> singleObserver(@NonNull Consumer<Disposable> disposableConsumer,
      @NonNull Consumer<? super T> onSuccess,
      @NonNull Consumer<? super Throwable> onError) {

    return new SingleObserver<>() {
      @Override
      public void onSubscribe(@NonNull Disposable d) {
        log.info("SUBSCRIBE ON {}", Thread.currentThread());
        try {
          disposableConsumer.accept(d);
        } catch (Throwable e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void onSuccess(@NonNull T t) {
        log.info("SUCCESS ON {}", Thread.currentThread());
        try {
          onSuccess.accept(t);
        } catch (Throwable e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void onError(@NonNull Throwable throwable) {
        log.info("ERROR ON {}", Thread.currentThread());
        try {
          onError.accept(throwable);
        } catch (Throwable e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

}
