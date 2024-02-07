package kyo.yaz.condominium.manager.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
public class HttpClientResponse {

  @JsonProperty("http_version")
  private final HttpVersion httpVersion;
  @JsonProperty("http_method")
  private final String httpMethod;
  @JsonProperty("url")
  private final String url;
  @JsonProperty("status_code")
  private final int statusCode;
  @JsonProperty("status_message")
  private final String statusMessage;
  @JsonProperty("headers")
  private final Map<String, String> headers;
  @JsonProperty("cookies")
  private final List<String> cookies;
  @JsonProperty("body")
  private final Buffer body;
  @JsonProperty("json_response")
  private final JsonObject jsonResponse;
  @JsonProperty("json_array_response")
  private final JsonArray jsonArrayResponse;
  @JsonProperty("text_response")
  private final String textResponse;
  @JsonProperty("response_time")
  private final long responseTime;


}