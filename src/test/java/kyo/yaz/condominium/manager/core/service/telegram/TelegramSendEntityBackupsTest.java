package kyo.yaz.condominium.manager.core.service.telegram;

import io.vertx.core.json.Json;
import java.util.concurrent.TimeUnit;
import kyo.yaz.condominium.manager.core.service.BackupService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class TelegramSendEntityBackupsTest {

  @Autowired
  private TelegramSendEntityBackups backups;

  @Autowired
  BackupService backupService;

  @Test
  void all() throws InterruptedException {

    final var single = backupService.allGz();

    final var testObserver = single.map(Json::encode)
        .doOnSuccess(System.out::println)
        .test();

    testObserver.await(120, TimeUnit.SECONDS);

    testObserver
        .assertComplete()
        .assertNoErrors();

  }
}