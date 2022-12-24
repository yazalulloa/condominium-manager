package kyo.yaz.condominium.manager.core.verticle;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import kyo.yaz.condominium.manager.core.domain.HttpClientRequest;
import kyo.yaz.condominium.manager.core.domain.HttpClientResponse;
import kyo.yaz.condominium.manager.core.domain.HttpLogConfig;
import kyo.yaz.condominium.manager.core.service.HttpLogging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class HttpClientVerticle extends AbstractVerticle {
    public static final String SEND = "http-send-request";

    private static final AtomicLong COUNTER = new AtomicLong(0);

    private WebClient webClient;
    private WebClient trustAll;

    private final HttpLogging httpLogging = new HttpLogging();

    @Override
    public void start() {
        webClient = WebClient.create(vertx, new WebClientOptions(config()));

        final var trustAllWebClientOptions = new WebClientOptions(config())
                .setTrustAll(true)
                .setVerifyHost(false)
                //.setSslEngineOptions(new InsecureJdkSSLEngineOptions())
                ;

        trustAll = WebClient.create(vertx, trustAllWebClientOptions);
        vertx.eventBus().<HttpClientRequest>consumer(SEND, m -> {
            sendHttpClientRequest(m.body())
                    .onComplete(ar -> {

                        if (ar.failed()) {
                            m.reply(ar.cause());
                        } else {
                            m.reply(ar.result());
                        }

                    });
        });
    }

    /*private static class InsecureOpenSSLEngineOptions extends OpenSSLEngineOptions {

        @Override
        public SslContextFactory sslContextFactory() {
            return new DefaultSslContextFactory(SslProvider.OPENSSL, true)
                    .trustManagerFactory(InsecureTrustManagerFactory.INSTANCE);
        }

    }

    private static class InsecureJdkSSLEngineOptions extends JdkSSLEngineOptions {
        @Override
        public SslContextFactory sslContextFactory() {
            return new DefaultSslContextFactory(SslProvider.JDK, false)
                    .trustManagerFactory(InsecureTrustManagerFactory.INSTANCE);
        }

    }*/

    private WebClient client(boolean trustAll) {
        return trustAll ? this.trustAll : webClient;
    }

    private Future<HttpClientResponse> sendHttpClientRequest(HttpClientRequest messageBody) {


        final var timeout = messageBody.timeoutTime() != 0 && messageBody.timeoutTimeUnit() != null
                ? messageBody.timeoutTimeUnit().toMillis(messageBody.timeoutTime())
                : config().getLong("timeout", 180000L);

        final var httpRequest = client(messageBody.trustAll())
                .requestAbs(messageBody.httpMethod(), messageBody.url())
                .timeout(timeout);

        messageBody.headers().forEach(httpRequest::putHeader);


        final var requestCounter = COUNTER.addAndGet(1);

        final var loggingEnabled = config().getBoolean("logging_enabled", true);
        final var prettyJson = config().getBoolean("pretty_json", false);
        final var responseLogConfig = Optional.ofNullable(messageBody.responseLogConfig()).orElseGet(() -> HttpLogConfig.builder().build());

        if (loggingEnabled) {
            httpLogging.logRequest(requestCounter, prettyJson, messageBody, httpRequest);
        }

        final var timestamp = System.currentTimeMillis();

        return send(httpRequest, messageBody)
                .map(response -> response(response, messageBody, timestamp))


                .onSuccess(response -> {
                    if (loggingEnabled) {
                        httpLogging.logResponse(requestCounter, prettyJson, messageBody, response, response.responseTime(), null, responseLogConfig);
                    }
                })
                .onFailure(e -> {
                    if (loggingEnabled) {
                        httpLogging.logResponse(requestCounter, prettyJson, messageBody, null, System.currentTimeMillis() - timestamp, e, responseLogConfig);
                    }
                });
        //.retryWhen(RetryWithDelay.retry(10, 100, TimeUnit.MILLISECONDS, t -> ClassUtil.isInstanceOf(t, DnsNameResolverTimeoutException.class, UnknownHostException.class, SSLException.class, SSLHandshakeException.class)));

    }


    private HttpClientResponse response(HttpResponse<Buffer> httpResponse, HttpClientRequest request, long timestamp) {

        final var headers = new HashMap<String, String>();


        httpResponse.headers().forEach(entry -> headers.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue()));
        final var responseTime = System.currentTimeMillis() - timestamp;

        return HttpClientResponse.builder()
                .httpVersion(httpResponse.version())
                .httpMethod(request.httpMethod().name())
                .url(request.url())
                .statusCode(httpResponse.statusCode())
                .statusMessage(httpResponse.statusMessage())
                .headers(headers)
                .cookies(httpResponse.cookies())
                .body(httpResponse.body())
                .responseTime(responseTime)
                .build();
    }


    private Future<HttpResponse<Buffer>> send(HttpRequest<Buffer> httpRequest, HttpClientRequest clientRequest) {

        final var body = Optional.ofNullable(clientRequest).map(HttpClientRequest::body).orElse(null);

        if (clientRequest == null || body == null) {

            return httpRequest.send();

        } else {

            if (HttpLogging.isJson(clientRequest.mediaType())) {
                if (body instanceof String) {

                    return httpRequest.sendJsonObject(new JsonObject((String) body));
                }

                if (body instanceof Buffer) {
                    final var str = body.toString();

                    return httpRequest.sendJson(str);
                }


                return httpRequest.sendJson(body);

            }

            if (HttpLogging.isText(clientRequest.mediaType())) {
                if (body instanceof Buffer) {
                    return httpRequest.sendBuffer((Buffer) body);
                }

                final var str = body.toString();
                return httpRequest.sendBuffer(Buffer.buffer(str));
            }

            if (HttpLogging.isFormData(clientRequest.mediaType())) {

                final var multiMap = Optional.ofNullable(clientRequest.body())
                        .map(Json::encode)
                        .map(JsonObject::new)
                        .map(jsonObject -> {
                            final var map = MultiMap.caseInsensitiveMultiMap();
                            jsonObject.forEach(entry -> {

                                final var value = Optional.ofNullable(entry.getValue()).map(Object::toString).orElse(null);
                                map.add(entry.getKey(), value);
                            });
                            return map;
                        })
                        .orElseGet(MultiMap::caseInsensitiveMultiMap);

                return httpRequest.sendForm(multiMap);
            }

            throw new RuntimeException("NOT_IMPLEMENTED");
        }
    }

    @Override
    public void stop() throws Exception {
        webClient.close();
        trustAll.close();
        webClient = null;
        trustAll = null;
    }
}
