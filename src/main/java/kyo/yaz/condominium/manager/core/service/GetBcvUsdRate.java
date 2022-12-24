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
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class GetBcvUsdRate {

    @Value("${app.bcv_url}")
    private String url;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private BcvUsdRateParser bcvUsdRateParser;

    private Mono<Document> docuument() {
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
        return docuument().flatMap(bcvUsdRateParser::parse);
    }
}
