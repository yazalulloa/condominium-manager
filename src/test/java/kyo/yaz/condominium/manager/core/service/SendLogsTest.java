package kyo.yaz.condominium.manager.core.service;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ExtendWith(VertxExtension.class)
@SpringBootTest
class SendLogsTest {

  @Autowired
  private NotificationService notificationService;

  @Test
  void test(VertxTestContext testContext) throws Throwable {

    notificationService.sendLogs("test")
        .subscribe(testContext::completeNow, testContext::failNow);

    testContext.awaitCompletion(10, TimeUnit.SECONDS);
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }
}