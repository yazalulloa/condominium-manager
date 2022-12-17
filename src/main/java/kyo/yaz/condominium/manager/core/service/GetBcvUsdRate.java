package kyo.yaz.condominium.manager.core.service;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import kyo.yaz.condominium.manager.core.domain.HttpClientRequest;
import kyo.yaz.condominium.manager.core.domain.HttpClientResponse;
import kyo.yaz.condominium.manager.core.domain.HttpLogConfig;
import kyo.yaz.condominium.manager.core.parser.BcvUsdRateParser;
import kyo.yaz.condominium.manager.core.verticle.HttpClientVerticle;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.concurrent.TimeUnit;

@Component()
public class GetBcvUsdRate {

    @Value("${app.bcv_url}")
    private String url;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private BcvUsdRateParser bcvUsdRateParser;

    public GetBcvUsdRate() {
        super();
    }

    private Mono<Document> jsoup() {
        return Mono.fromCallable(() -> {
            return Jsoup.parse(new URL(url), (int) TimeUnit.SECONDS.toMillis(10));
        });
    }
    private Mono<Document> vertx() {
        final HttpClientRequest httpClientRequest = HttpClientRequest.get(url)
                .toBuilder()
                .trustAll(true)
                .responseLogConfig(HttpLogConfig.builder()
                        .showBody(false)
                        .build())
                .build();

        return Mono.create(emitter -> {

            eventBus.<HttpClientResponse>request(HttpClientVerticle.SEND, httpClientRequest)
                    .map(Message::body)
                    .map(HttpClientResponse::body)
                    .map(Buffer::toString)
                    .map(Jsoup::parse)
                    .onComplete(ar -> {

                        if (ar.failed()) {
                            emitter.error(ar.cause());
                        } else {
                            emitter.success(ar.result());
                        }

                    });
        });

    }

    public Mono<Rate> newRate() {
/*
        Mono.fromCallable(() -> {
            Jsoup.parse(new URL(""), (int) TimeUnit.SECONDS.toMillis(60));
        })*/

        return
                //document()
                vertx()
                        .flatMap(bcvUsdRateParser::parse);

        /*return getRate()
                .flatMap(bcvUsdRateParser::parse);*/
    }
}
