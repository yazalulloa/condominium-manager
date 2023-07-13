package kyo.yaz.condominium.manager.core.service.entity;

import io.vertx.core.json.Json;
import io.vertx.core.json.jackson.DatabindCodec;
import java.util.concurrent.TimeUnit;
import kyo.yaz.condominium.manager.core.util.JacksonUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class CalculateReceiptInfoTest {

  @Autowired
  private CalculateReceiptInfo calculateReceiptInfo;

  @Test
  void calculate() throws InterruptedException {
    JacksonUtil.defaultConfig(DatabindCodec.mapper());
    JacksonUtil.defaultConfig(DatabindCodec.prettyMapper());

    final var testObserver = calculateReceiptInfo.calculate(55L)
        .map(Json::encodePrettily)
        .doOnSuccess(System.out::println)
        .test();

    testObserver.await(10, TimeUnit.MINUTES);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }

}