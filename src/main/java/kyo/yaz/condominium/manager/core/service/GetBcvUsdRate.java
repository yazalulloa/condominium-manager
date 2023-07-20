package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.buffer.Buffer;
import kyo.yaz.condominium.manager.core.domain.HttpClientRequest;
import kyo.yaz.condominium.manager.core.domain.HttpClientResponse;
import kyo.yaz.condominium.manager.core.domain.HttpLogConfig;
import kyo.yaz.condominium.manager.core.parser.BcvUsdRateParser;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GetBcvUsdRate {

    private final String url;
    private final BcvUsdRateParser bcvUsdRateParser;
    private final HttpService httpService;

    @Autowired
    public GetBcvUsdRate(@Value("${app.bcv_url}") String url, BcvUsdRateParser bcvUsdRateParser, HttpService httpService) {
        this.url = url;
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
    }

    public Single<Rate> newRate() {
        return document().map(bcvUsdRateParser::parse);
    }
}
