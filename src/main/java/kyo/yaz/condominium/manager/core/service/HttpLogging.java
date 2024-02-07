package kyo.yaz.condominium.manager.core.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.client.HttpRequest;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kyo.yaz.condominium.manager.core.domain.HttpClientRequest;
import kyo.yaz.condominium.manager.core.domain.HttpClientResponse;
import kyo.yaz.condominium.manager.core.domain.HttpLogConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

public class HttpLogging {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public static boolean isJson(MediaType type) {
    final var mediaType = type == null ? MediaType.APPLICATION_JSON : type;

    return mediaType.equals(MediaType.APPLICATION_JSON)
        || mediaType.equals(MediaType.APPLICATION_PROBLEM_JSON);
  }

  public static boolean isText(MediaType type) {
    final var mediaType = type == null ? MediaType.APPLICATION_JSON : type;

    return mediaType.equals(MediaType.TEXT_PLAIN);
  }

  public static boolean isFormData(MediaType type) {
    final var mediaType = type == null ? MediaType.APPLICATION_JSON : type;

    return mediaType.equals(MediaType.MULTIPART_FORM_DATA);
  }

  public void logRequest(long requestCounter, boolean prettyJson, HttpClientRequest clientRequest,
      HttpRequest<Buffer> httpRequest) {

    try {
      final var builder = new StringBuilder("\n").append(String.format("[HTTP_CLIENT_REQUEST_%s]", requestCounter))
          .append(clientRequest.trustAll() ? " TRUST_ALL" : "").append("\n");
      final var logConfig = Optional.ofNullable(clientRequest.requestLogConfig())
          .orElseGet(() -> HttpLogConfig.builder().build());
      final var httpMethod = clientRequest.httpMethod().name();

      final var uri = url(clientRequest.url(), logConfig.excludeQueryParams());

      final var method = clientRequest.httpMethod();

      builder.append(requestCounter).append(" > ").append(httpMethod).append(" ").append(uri).append("\n");

      httpRequest.headers().forEach(entry -> {
        final var header = logConfig.excludeHeaders().contains(entry.getKey()) ? "XXXXXX" : entry.getValue();

        builder.append(requestCounter).append(" > ")
            .append(entry.getKey())
            .append(": ").append(header).append("\n");

      });

      if (method.equals(HttpMethod.PUT) || method.equals(HttpMethod.POST)) {

        if (clientRequest.multipartForm() != null) {

          clientRequest.multipartForm().forEach(formDataPart -> {

            final var type = Objects.requireNonNullElse(formDataPart.isText(), false) ? "[text]"
                : formDataPart.isAttribute() ? "[attr]"
                    : formDataPart.isFileUpload() ? "[file-upload]" : null;

            final var str = Stream.of(type, formDataPart.name(), formDataPart.pathname(), formDataPart.value(),
                    formDataPart.filename(), formDataPart.mediaType())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
            builder.append(str).append("\n");
          });


        } else if (clientRequest.body() != null && logConfig.showBody()) {

          if (isJson(clientRequest.mediaType())) {
            final var json = parseJson(prettyJson, clientRequest.body(), logConfig.excludeFields());
            builder.append(json).append("\n");
          } else {
            builder.append(clientRequest.body().toString()).append("\n");
          }
        }
      }

      logger.info("REQUEST: {}", builder);


    } catch (Exception e) {
      logger.error("LOGGING_REQUEST", e);
    }
  }

  public void logResponse(long requestCounter, boolean prettyJson, HttpClientRequest clientRequest,
      HttpClientResponse response, long responseTime, Throwable throwable, HttpLogConfig logConfig) {

    try {

      final var builder = new StringBuilder("\n").append(String.format("[HTTP_CLIENT_RESPONSE_%s]", requestCounter))
          .append(clientRequest.trustAll() ? " TRUST_ALL" : "").append("\n");

      final var uri = url(clientRequest.url(), logConfig.excludeQueryParams());

      final var direction = " < ";
      if (response != null) {

        builder.append(requestCounter).append(direction).append(response.statusCode()).append(" ")
            .append(response.httpMethod())
            .append(" ").append(response.httpVersion().name()).append(" ").append(uri).append("\n")
            .append(requestCounter).append(direction).append(response.httpMethod()).append(" ").append(uri).append("\n")
            .append(requestCounter).append(direction).append(responseTime).append("ms").append("\n");

        if (response.statusMessage() != null && !response.statusMessage().isEmpty()) {
          builder.append(requestCounter).append(direction).append(response.statusMessage()).append("\n");
        }

        response.headers().forEach((key, value) -> {

          final var header = logConfig.excludeHeaders().contains(key) ? "XXXXXXX" : value;

          builder.append(requestCounter).append(direction).append(key)
              .append(": ").append(header).append("\n");

        });

        if (response.cookies() != null && !response.cookies().isEmpty()) {
          builder.append(requestCounter).append(direction).append("Cookies: ")
              .append(String.join(",", response.cookies())).append("\n");
        }

        final var body = Optional.ofNullable(response.body())
            .map(buffer -> {

              if (response.statusCode() == 200 & !logConfig.showBody()) {
                return "DO_NOT_SHOW_BODY";
              }

              final var contentType = response.headers().getOrDefault("content-type", "");

              if (contentType.contains("application/json")) {
                return parseJson(prettyJson, buffer.toJson(), logConfig.excludeFields());
              }

              return buffer.toString();

            })
            .orElse("");

        builder.append(body).append("\n");

      } else {

        builder.append(requestCounter).append(direction).append(clientRequest.httpMethod()).append(" ").append(uri)
            .append("\n")
            .append(requestCounter).append(direction).append(responseTime).append("ms").append("\n");
      }

      if (throwable != null) {
        builder.append(requestCounter).append(direction).append("ERROR ").append(throwable.getClass())
            .append(" ")
            .append(throwable.getMessage())
            .append("\n");
      }

      logger.info("RESPONSE {}", builder);

    } catch (Exception e) {
      logger.error("LOGGING_RESPONSE", e);
    }

  }

  private String url(String url, Set<String> exclude) {
    if (exclude.isEmpty()) {
      return url;
    }

    return url;

        /*final var uriBuilder = UriBuilder.fromUri(url);

        exclude.forEach(str -> uriBuilder.replaceQueryParam(str, ""));

        return uriBuilder.build().toString();*/
  }

  private String parseJson(boolean prettyJson, Object obj, Set<String> excludes) {
    try {

      if (obj instanceof Buffer) {
        try {
          parseJson(prettyJson, ((Buffer) obj).toJsonObject(), excludes);
        } catch (Exception e) {
          parseJson(prettyJson, ((Buffer) obj).toJsonArray(), excludes);
        }
      }

      if (excludes.isEmpty()) {
        if (obj instanceof JsonObject) {
          return prettyJson ? ((JsonObject) obj).encodePrettily() : ((JsonObject) obj).encode();
        }

        if (obj instanceof JsonArray) {
          return prettyJson ? ((JsonArray) obj).encodePrettily() : ((JsonArray) obj).encode();
        }

        return prettyJson ? Json.encodePrettily(obj) : Json.encode(obj);
      }

      final var treeNode = DatabindCodec.mapper().valueToTree(obj);
      if (treeNode.isArray()) {
        final var arrayNode = (ArrayNode) treeNode;
        replace(arrayNode, excludes);
      } else {
        replace((ObjectNode) treeNode, excludes);
      }

      return prettyJson ? treeNode.toPrettyString() : treeNode.toString();
    } catch (Exception e) {
      //logger.error("ERROR_PARSING_BODY", e);
      return obj.toString();
    }


  }

  private void replace(ArrayNode arrayNode, Set<String> excludes) {
    for (JsonNode jsonNode : arrayNode) {
      if (jsonNode.isArray()) {
        replace((ArrayNode) jsonNode, excludes);
      } else if (jsonNode.isObject()) {
        replace((ObjectNode) jsonNode, excludes);
      }
    }

  }

  private void replace(ObjectNode objectNode, Set<String> excludes) {
    for (String exclude : excludes) {

      if (exclude.contains(".")) {
        final var indexOf = exclude.indexOf(".");

        final var field = exclude.substring(0, indexOf);

        final var jsonNode = objectNode.get(field);

        if (jsonNode != null) {
          if (jsonNode.isArray()) {
            replace((ArrayNode) jsonNode, Set.of(exclude.substring(indexOf + 1)));
          } else if (jsonNode.isObject()) {
            replace((ObjectNode) jsonNode, Set.of(exclude.substring(indexOf + 1)));
          }
        }

      } else {

        final var jsonNode = objectNode.get(exclude);
        if (jsonNode != null && !jsonNode.isEmpty() && !jsonNode.isMissingNode()) {
          objectNode.put(exclude, "XXXXXX");
        }
      }

    }


  }
}
