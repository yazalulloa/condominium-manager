package kyo.yaz.condominium.manager.core.job;

import java.util.concurrent.TimeUnit;
import kyo.yaz.condominium.manager.core.service.DeleteDirAfterDelay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeleteTmpDirJob {

  private final DeleteDirAfterDelay deleteDirAfterDelay;


  @Async
  @Scheduled(initialDelay = 5, fixedRate = 86400, timeUnit = TimeUnit.SECONDS)
  public void runAsStart() {
    deleteDirAfterDelay.deleteTmpNow();
  }
}
