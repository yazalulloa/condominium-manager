package kyo.yaz.condominium.manager.core.job;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import kyo.yaz.condominium.manager.core.service.SaveNewBcvRate;
import kyo.yaz.condominium.manager.core.util.RxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BcvRateJob {

  private final SaveNewBcvRate saveNewBcvRate;
  private final String expression;

  @Autowired
  public BcvRateJob(SaveNewBcvRate saveNewBcvRate, @Value("${app.bcv_job_cron_expression}") String expression) {
    this.saveNewBcvRate = saveNewBcvRate;
    this.expression = expression;
  }

  @Async
  @Scheduled(initialDelay = 60, fixedRate = 86400, timeUnit = TimeUnit.SECONDS)
  public void runAsStart() {
    saveNewBcvRate();
  }

  @Async
  @Scheduled(cron = "${app.bcv_job_cron_expression}")
  public void scheduleFixedRateTaskAsync() {
    //log.info("CRON_BCV_JOB");

    final var cronExpression = CronExpression.parse(expression);
    var result = cronExpression.next(LocalDateTime.now());
    // log.info("NEXT {}", result);
    // log.info("EXPRESSION {}", expression);
    //log.info("CRON_EXPRESSION {}", cronExpression);

    saveNewBcvRate();
  }

  private void saveNewBcvRate() {
    //log.info("RUN_JOB {}", Thread.currentThread());

    saveNewBcvRate.saveNewRate()
        .doOnError(throwable -> log.error("ERROR", throwable))
        .retry()
        .subscribe(RxUtil.singleObserver(bool -> log.info("NEW_RATE_SAVED {}", bool),
            throwable -> log.error("ERROR", throwable)));
  }
}
