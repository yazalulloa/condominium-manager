package kyo.yaz.condominium.manager.core.job;

import kyo.yaz.condominium.manager.core.service.entity.EmailConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailConfigCheckJob {

    private final EmailConfigService service;

    @Autowired
    public EmailConfigCheckJob(EmailConfigService service) {
        this.service = service;
    }


    @Async
    @Scheduled(cron = "${app.email_check_job_cron_expression}")
    public void emailCheck() {

        service.checkAll()
                .subscribe(() -> log.info("SUCCESS"),
                        throwable -> log.error("ERROR", throwable));
    }
}
