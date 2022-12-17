package kyo.yaz.condominium.manager.core.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.http.HttpMethod;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
public class HttpClientRequest {

    @JsonProperty("http_method")
    private final HttpMethod httpMethod;
    @JsonProperty("url")
    private final String url;

    @Builder.Default
    @JsonProperty("headers")
    private final Map<String, String> headers = new HashMap<>();

    @JsonProperty("body")
    private final Object body;

    @JsonProperty("media_type")
    private final MediaType mediaType;

    @JsonProperty("request_log_config")
    private final HttpLogConfig requestLogConfig;

    @JsonProperty("response_log_config")
    private final HttpLogConfig responseLogConfig;

    @JsonProperty("trust_all")
    private final boolean trustAll;

    @JsonProperty("timeout_time")
    private final long timeoutTime;

    @JsonProperty("timeout_time_unit")
    private final TimeUnit timeoutTimeUnit;

    public static HttpClientRequest of(HttpMethod httpMethod, String url, Object body) {
        return HttpClientRequest.builder().httpMethod(httpMethod).url(url).body(body).build();
    }

    public static HttpClientRequest of(HttpMethod httpMethod, String url) {
        return HttpClientRequest.builder().httpMethod(httpMethod).url(url).build();
    }
    public static HttpClientRequest options(String url) {
        return of(HttpMethod.OPTIONS, url);
    }

    public static HttpClientRequest post(String url) {
        return of(HttpMethod.POST, url);
    }

    public static HttpClientRequest post(String url, Object body) {
        return of(HttpMethod.POST, url, body);
    }

    public static HttpClientRequest get(String url) {
        return of(HttpMethod.GET, url);
    }

    public static HttpClientRequest get(String url, Object body) {
        return of(HttpMethod.GET, url, body);
    }

    public static HttpClientRequest put(String url) {
        return of(HttpMethod.PUT, url);
    }

    public static HttpClientRequest put(String url, Object body) {
        return of(HttpMethod.PUT, url, body);
    }

    public static HttpClientRequest delete(String url) {
        return of(HttpMethod.DELETE, url);
    }

    public static HttpClientRequest delete(String url, Object body) {
        return of(HttpMethod.DELETE, url, body);
    }
}

