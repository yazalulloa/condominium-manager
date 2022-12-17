package kyo.yaz.condominium.manager.core.service;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

class GetBcvUsdRateTest {

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

}