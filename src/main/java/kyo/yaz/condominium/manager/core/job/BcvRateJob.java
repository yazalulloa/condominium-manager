package kyo.yaz.condominium.manager.core.job;

import kyo.yaz.condominium.manager.core.service.SaveNewBcvRate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
public class BcvRateJob {

    private final SaveNewBcvRate saveNewBcvRate;

    @Autowired
    public BcvRateJob(SaveNewBcvRate saveNewBcvRate) {
        this.saveNewBcvRate = saveNewBcvRate;
    }


    @Async
    @Scheduled(cron = "${app.bcv_job_cron_expression}")
    public void scheduleFixedRateTaskAsync() {

        log.info("RUN JOB");

        saveNewBcvRate.saveNewRate()
                .subscribeOn(Schedulers.parallel())
                .doOnError(throwable -> log.error("ERROR", throwable))
                .retry()
                .subscribe(bool -> log.info("NEW_RATE_SAVED {}", bool),
                        throwable -> log.error("ERROR", throwable));

    }
}