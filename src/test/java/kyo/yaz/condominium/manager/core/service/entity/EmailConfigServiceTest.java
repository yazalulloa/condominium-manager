package kyo.yaz.condominium.manager.core.service.entity;

import io.vertx.core.json.Json;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class EmailConfigServiceTest {

  @Autowired
  private EmailConfigService service;

  @Test
  void listForComboBox() throws InterruptedException {

    final var single = service.listForComboBox();

    final var testObserver = single.map(Json::encode)
        .doOnSuccess(System.out::println)
        .test();

    testObserver.await(10, TimeUnit.SECONDS);

    testObserver
        .assertComplete()
        .assertNoErrors();

  }

}