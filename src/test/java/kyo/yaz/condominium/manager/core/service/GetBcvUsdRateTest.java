package kyo.yaz.condominium.manager.core.service;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.vertx.core.json.Json;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@RunWith(SpringRunner.class)
@SpringBootTest
class GetBcvUsdRateTest {

  @Autowired
  private GetBcvUsdRate getBcvUsdRate;

  @Test
  void test() {
    final var httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .responseTimeout(Duration.ofMillis(5000))
        .doOnConnected(conn ->
            conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

    final var webClient = WebClient.builder()
        .baseUrl("http://www.bcv.org.ve")
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();

    final var block = webClient.get().retrieve().bodyToMono(String.class)
        .block();

    System.out.println("html: " + block);
  }

  @Test
  void newRate() throws InterruptedException {

    final var testObserver = getBcvUsdRate.newRate()
        .map(Json::encode)
        .doOnSuccess(System.out::println)
        .repeat(10)
        .test();

    testObserver.await(120, TimeUnit.SECONDS);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }


}