package kyo.yaz.condominium.manager.core.util.rx;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.BiConsumer;
import io.reactivex.rxjava3.functions.Function;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RetryWithDelay implements Function<Flowable<? extends Throwable>, Flowable<?>> {

    private final int maxRetryCount;
    private final int retryDelay;
    private final TimeUnit timeUnit;
    private final Function<Throwable, Boolean> validator;
    private int retryCount;
    private BiConsumer<Integer, Integer> retryCounterConsumer;

    private RetryWithDelay(int maxRetryCount, int retryDelay, TimeUnit timeUnit, Function<Throwable, Boolean> validator,
                           BiConsumer<Integer, Integer> retryCounterConsumer) {
        this.maxRetryCount = maxRetryCount;
        this.retryDelay = retryDelay;
        this.timeUnit = Objects.requireNonNull(timeUnit, "TIME_UNIT_MUST_NOT_BE_NULL");
        this.validator = validator;
        this.retryCount = 0;
        this.retryCounterConsumer = retryCounterConsumer;
    }

    public static RetryWithDelay retry(int maxRetryCount, int retryDelay, TimeUnit timeUnit) {
        return new RetryWithDelay(maxRetryCount, retryDelay, timeUnit, null, null);
    }

    public static RetryWithDelay retry(int maxRetryCount, int retryDelay, TimeUnit timeUnit,
                                       Function<Throwable, Boolean> validator) {
        return new RetryWithDelay(maxRetryCount, retryDelay, timeUnit, validator, null);
    }

    public static RetryWithDelay retry(int maxRetryCount, int retryDelay, TimeUnit timeUnit,
                                       Function<Throwable, Boolean> validator, BiConsumer<Integer, Integer> retryCounterConsumer) {
        return new RetryWithDelay(maxRetryCount, retryDelay, timeUnit, validator, retryCounterConsumer);
    }

    public static RetryWithDelay retry(int retryDelay, TimeUnit timeUnit) {
        return new RetryWithDelay(0, retryDelay, timeUnit, null, null);
    }

    public static RetryWithDelay retry(int retryDelay, TimeUnit timeUnit, Function<Throwable, Boolean> validator) {
        return new RetryWithDelay(0, retryDelay, timeUnit, validator, null);
    }

    @Override
    public Flowable<?> apply(final Flowable<? extends Throwable> attempts) {

        return attempts.flatMap(throwable -> {

            if (validator == null || validator.apply(throwable)) {
                if (maxRetryCount == 0 || ++retryCount < maxRetryCount) {
                    if (retryCounterConsumer != null) {
                        retryCounterConsumer.accept(retryCount, maxRetryCount);
                    }

                    return Flowable.timer(retryDelay, timeUnit);
                }
            }

            return Flowable.error(throwable);
        });
    }
}