package kyo.yaz.condominium.manager.ui.views.receipt.service;

import io.vertx.core.json.jackson.DatabindCodec;
import java.util.concurrent.TimeUnit;
import kyo.yaz.condominium.manager.core.service.entity.ReceiptService;
import kyo.yaz.condominium.manager.core.util.JacksonUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class BuildReceiptPdfsTest {

  @Autowired
  BuildReceiptPdfs buildReceiptPdfs;
  @Autowired
  ReceiptService receiptService;

  @Test
  void calculate() throws InterruptedException {
    JacksonUtil.defaultConfig(DatabindCodec.mapper());
    JacksonUtil.defaultConfig(DatabindCodec.prettyMapper());

    final var testObserver = receiptService.get(55L)
        .flatMap(buildReceiptPdfs::pdfItems)
        //.map(Json::encodePrettily)
        .doOnSuccess(System.out::println)
        .test();

    testObserver.await(10, TimeUnit.MINUTES);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }
}