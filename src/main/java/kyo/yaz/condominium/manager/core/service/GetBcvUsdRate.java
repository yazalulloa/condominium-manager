package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import kyo.yaz.condominium.manager.core.domain.HttpClientRequest;
import kyo.yaz.condominium.manager.core.domain.HttpClientResponse;
import kyo.yaz.condominium.manager.core.domain.HttpLogConfig;
import kyo.yaz.condominium.manager.core.parser.BcvUsdRateParser;
import kyo.yaz.condominium.manager.core.vertx.VertxHandler;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GetBcvUsdRate {

    private final String url;
    private final VertxHandler vertxHandler;
    private final EventBus eventBus;
    private final BcvUsdRateParser bcvUsdRateParser;
    private final HttpService httpService;

    @Autowired
    public GetBcvUsdRate(@Value("${app.bcv_url}") String url, VertxHandler vertxHandler, EventBus eventBus, BcvUsdRateParser bcvUsdRateParser, HttpService httpService) {
        this.url = url;
        this.vertxHandler = vertxHandler;
        this.eventBus = eventBus;
        this.bcvUsdRateParser = bcvUsdRateParser;
        this.httpService = httpService;
    }

    private Single<String> document() {
        final HttpClientRequest httpClientRequest = HttpClientRequest.get(url)
                .toBuilder()
                .trustAll(true)
                .responseLogConfig(HttpLogConfig.builder()
                        .showBody(false)
                        .build())
                .build();

        return httpService.send(httpClientRequest)
                .map(HttpClientResponse::body)
                .map(Buffer::toString);


      /*  return Single.create(emitter -> {

            eventBus.<HttpClientResponse>request(HttpClientVerticle.SEND, httpClientRequest)
                    .onComplete(ar -> {

                        if (ar.failed()) {
                            emitter.onError(ar.cause());
                        } else {
                            final var document = Jsoup.parse(ar.result().body().toString());
                            emitter.onSuccess(document);
                        }

                    });
        });*/

    }

    public Single<Rate> newRate() {
        return document().map(bcvUsdRateParser::parse);
    }
}
