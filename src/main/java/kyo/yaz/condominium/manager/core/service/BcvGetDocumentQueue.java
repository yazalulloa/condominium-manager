package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Component
public class BcvGetDocumentQueue {

    private static final Waiter waiter = new Waiter();

    private final GetBcvUsdRate getBcvUsdRate;

    public void getNewRate(Handler<AsyncResult<Rate>> resultHandler) {

        if (waiter.isAvailable()) {
            //log.info("QUEUE_IS_AVAILABLE");
            waiter.add(resultHandler);
            resolve();
        } else {
            log.info("QUEUE_IS_NOT_AVAILABLE");
            resultHandler.handle(Future.succeededFuture());
        }
    }

    private void resolve() {

        getBcvUsdRate.newRate()
                //.delay(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .doAfterTerminate(waiter::restart)
                .subscribe(
                        rate -> waiter.handler.handle(Future.succeededFuture(rate)),
                        error -> waiter.handler.handle(Future.failedFuture(error))
                );

    }

    @SuperBuilder(toBuilder = true)
    @Accessors(fluent = true)
    @ToString
    @Getter
    public static class Waiter {

        private final AtomicBoolean available;
        private final AtomicInteger counter;
        private ZonedDateTime last;
        private Handler<AsyncResult<Rate>> handler;

        public Waiter() {
            this.available = new AtomicBoolean(true);
            this.counter = new AtomicInteger(0);
        }

        public boolean isAvailable() {
            return available().get();
        }

        public void disable() {
            available().set(false);
        }

        public void restart() {
            available().set(true);
            last = null;
            handler = null;
        }

        public boolean add(Handler<AsyncResult<Rate>> resultHandler) {
            available.set(false);
            this.last = DateUtil.nowZonedWithUTC();
            this.handler = resultHandler;
            return true;
        }
    }

}
