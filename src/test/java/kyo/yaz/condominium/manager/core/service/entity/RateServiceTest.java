package kyo.yaz.condominium.manager.core.service.entity;

import io.vertx.core.json.Json;
import java.util.concurrent.TimeUnit;
import kyo.yaz.condominium.manager.core.domain.Currency;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class RateServiceTest {

  @Autowired
  private RateService service;

  @Test
  void last() throws InterruptedException {

    final var single = service.last(Currency.USD, Currency.VED);

    final var testObserver = single.map(Json::encode)
        .doOnSuccess(System.out::println)
        .test();

    testObserver.await(60, TimeUnit.SECONDS);

    testObserver
        .assertComplete()
        .assertNoErrors();

  }


  @Test
  void download() throws InterruptedException {

    final var download = service.download();

    final var testObserver = download
        .doOnSuccess(System.out::println)
        //.flatMapCompletable(service::upload)
        .test();

    testObserver.await(120, TimeUnit.SECONDS);

    testObserver
        .assertComplete()
        .assertNoErrors();

  }

  @Test
  void upload() throws InterruptedException {

    final var upload = service.upload("rates.json.gz");

    final var testObserver = upload
        .andThen(service.countAll())
        .doOnSuccess(System.out::println)
        //.flatMapCompletable(service::upload)
        .test();

    testObserver.await(120, TimeUnit.SECONDS);

    testObserver
        .assertComplete()
        .assertNoErrors();

  }
}