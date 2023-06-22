package kyo.yaz.condominium.manager.core.job;

import kyo.yaz.condominium.manager.core.service.entity.EmailConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailConfigCheckJob {

  private final EmailConfigService service;


  @Async
  @Scheduled(cron = "${app.email_check_job_cron_expression}")
  public void emailCheck() {

    service.checkAll()
        .subscribe(() -> log.info("SUCCESS"),
            throwable -> log.error("ERROR", throwable));
  }
}
