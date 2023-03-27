package kyo.yaz.condominium.manager.core.job;

import kyo.yaz.condominium.manager.core.service.DeleteDirAfterDelay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DeleteTmpDirJob {

    private final DeleteDirAfterDelay deleteDirAfterDelay;

    @Autowired
    public DeleteTmpDirJob(DeleteDirAfterDelay deleteDirAfterDelay) {
        this.deleteDirAfterDelay = deleteDirAfterDelay;
    }

    @Async
    @Scheduled(initialDelay = 5, fixedRate = 86400, timeUnit = TimeUnit.SECONDS)
    public void runAsStart() {
        deleteDirAfterDelay.deleteTmp();
    }
}
