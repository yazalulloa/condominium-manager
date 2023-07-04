package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.http.HttpMethod;
import kyo.yaz.condominium.manager.core.domain.HttpClientRequest;
import kyo.yaz.condominium.manager.core.domain.HttpClientResponse;
import org.springframework.http.MediaType;

public interface HttpService {

  Single<HttpClientResponse> send(HttpClientRequest request);

  default Single<HttpClientResponse> get(String url) {
    return send(HttpMethod.GET, url);
  }

  default Single<HttpClientResponse> head(String url) {
    return send(HttpMethod.HEAD, url);
  }

  default Single<HttpClientResponse> options(String url) {
    return send(HttpMethod.OPTIONS, url);
  }

  default Single<HttpClientResponse> delete(String url) {
    return send(HttpMethod.DELETE, url);
  }

  default Single<HttpClientResponse> send(HttpMethod httpMethod, String url) {
    return send(HttpClientRequest.builder()
        .httpMethod(httpMethod)
        .url(url)
        .build());
  }

  default Single<HttpClientResponse> send(HttpMethod httpMethod, String url, Object object) {
    return send(HttpClientRequest.builder()
        .httpMethod(httpMethod)
        .url(url)
        .body(object)
        .build());
  }

  default Single<HttpClientResponse> text(HttpMethod httpMethod, String url, String object) {
    return send(HttpClientRequest.builder()
        .httpMethod(httpMethod)
        .url(url)
        .body(object)
        .mediaType(MediaType.TEXT_PLAIN)
        .build());
  }


  default Single<HttpClientResponse> json(HttpMethod httpMethod, String url, String object) {
    return send(HttpClientRequest.builder()
        .httpMethod(httpMethod)
        .url(url)
        .body(object)
        .mediaType(MediaType.APPLICATION_JSON)
        .build());
  }
}
