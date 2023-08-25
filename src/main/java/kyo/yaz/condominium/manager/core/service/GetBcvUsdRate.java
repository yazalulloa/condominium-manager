package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.http.HttpMethod;
import kyo.yaz.condominium.manager.core.domain.*;
import kyo.yaz.condominium.manager.core.parser.BcvUsdRateParser;
import kyo.yaz.condominium.manager.core.service.entity.RateService;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GetBcvUsdRate {

  private final String url;
  private final BcvUsdRateParser bcvUsdRateParser;
  private final HttpService httpService;
  private final RateService rateService;

  @Autowired
  public GetBcvUsdRate(@Value("${app.bcv_url}") String url, BcvUsdRateParser bcvUsdRateParser, HttpService httpService,
      RateService rateService) {
    this.url = url;
    this.bcvUsdRateParser = bcvUsdRateParser;
    this.httpService = httpService;
    this.rateService = rateService;
  }

  private Single<HttpClientResponse> send(HttpMethod httpMethod) {
    return httpService.send(httpRequest(httpMethod));
  }

  private HttpClientRequest httpRequest(HttpMethod httpMethod) {
    return HttpClientRequest.builder()
        .httpMethod(httpMethod)
        .url(url)
        .trustAll(true)
        .responseLogConfig(HttpLogConfig.builder()
            .showBody(false)
            .build())
        .build();
  }

  public Single<BcvUsdRateResult> newRate() {
    final var newRateSingle = send(HttpMethod.GET)
        .map(bcvUsdRateParser::parse)
        .map(newRate -> new BcvUsdRateResult(BcvUsdRateResult.State.NEW_RATE, newRate));

    return rateService.last(Currency.USD, Currency.VED)
        .filter(rate -> CollectionUtils.isNotEmpty(rate.etags()))
        .flatMapSingle(rate -> {

          return send(HttpMethod.HEAD)
              .flatMap(response -> {
                final var etag = response.headers().get("etag");
                final var lastModified = response.headers().get("last-modified");

                if (rate.etags().contains(etag)) {
                  return Single.just(new BcvUsdRateResult(BcvUsdRateResult.State.ETAG_IS_SAME));
                } else {
                  return newRateSingle;
                }

              });

        })
        .switchIfEmpty(newRateSingle);
  }

   /* public Single<Rate> newRate() {
        return send(HttpMethod.GET).map(bcvUsdRateParser::parse);
    }*/
}
