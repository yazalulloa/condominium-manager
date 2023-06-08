package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.eventbus.DeliveryOptions;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import kyo.yaz.condominium.manager.core.domain.HttpClientRequest;
import kyo.yaz.condominium.manager.core.domain.HttpClientResponse;
import kyo.yaz.condominium.manager.core.verticle.HttpClientVerticle;
import kyo.yaz.condominium.manager.core.vertx.VertxHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class HttpServiceImpl implements HttpService {

  private final VertxHandler handler;

  public Single<HttpClientResponse> send(HttpClientRequest request) {

    final var timeoutTime = Optional.of(request.timeoutTime())
        .filter(o -> o > 1)
        .orElse(3L);

    final var timeUnit = Optional.ofNullable(request.timeoutTimeUnit())
        .orElse(TimeUnit.MINUTES);

    return handler.get(HttpClientVerticle.SEND, request,
        new DeliveryOptions().setSendTimeout(timeUnit.toMillis(timeoutTime)));
  }
}
