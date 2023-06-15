package kyo.yaz.condominium.manager.core.verticle;

import com.fasterxml.jackson.databind.JsonNode;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.util.Optional;
import kyo.yaz.condominium.manager.core.domain.HttpClientRequest;
import kyo.yaz.condominium.manager.core.domain.HttpClientResponse;
import kyo.yaz.condominium.manager.core.domain.telegram.EditMessageText;
import kyo.yaz.condominium.manager.core.domain.telegram.SendDocument;
import kyo.yaz.condominium.manager.core.domain.telegram.SendMessage;
import kyo.yaz.condominium.manager.core.service.HttpService;
import kyo.yaz.condominium.manager.core.service.TelegramCommandResolver;
import kyo.yaz.condominium.manager.core.vertx.VertxHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class TelegramVerticle extends BaseVerticle {

  public static final String WEB_HOOK = "web-hook";

  public static final String SEND_MESSAGE = address("send-message-telegram-api");
  public static final String EDIT_MESSAGE_TEXT = address("edit-message-text-telegram-api");
  public static final String SEND_DOCUMENT = address("send-document-telegram-api");
  public static final String GET_WEBHOOK_INFO = address("get-webhook-info-telegram-api");
  public static final String DELETE_WEBHOOK = address("delete-webhook-telegram-api");
  public static final String SET_WEBHOOK = address("post-set-webhook-telegram-api");
  public static final String SET_DEFAULT_WEBHOOK = address("set-default-webhook-telegram-api");
  public static final String GET_START_URL = address("get-start-url-telegram-api");

  private final HttpService httpService;
  private final VertxHandler vertxHandler;


  private final TelegramCommandResolver commandResolver;

  @Override
  public void start() throws Exception {
    super.start();
    vertx.eventBus().<JsonNode>consumer(WEB_HOOK, m -> {
      final var json = m.body();

      log.info("JSON {}", json.toString());

      subscribe(commandResolver.resolve(json));
    });

    eventBusConsumer(SEND_MESSAGE, this::sendMessage);
    eventBusConsumer(EDIT_MESSAGE_TEXT, this::editMessageText);
    eventBusConsumer(SEND_DOCUMENT, this::sendDocument);
    eventBusConsumerEmptyBody(GET_WEBHOOK_INFO, this::getWebhookInfo);
    eventBusConsumerEmptyBody(DELETE_WEBHOOK, this::deleteWebhook);
    eventBusConsumer(SET_WEBHOOK, this::setWebhook);
    eventBusConsumerEmptyBody(SET_DEFAULT_WEBHOOK, this::setDefaultWebhook);
    vertx.eventBus().<String>consumer(GET_START_URL, m -> m.reply(config().getString("start_url") + m.body()));
  }

  private UriBuilder uriBuilder() {
    return UriComponentsBuilder.fromUriString(config().getString("url"));
  }

  private String url(String path) {
    return uriBuilder().path(path).build().toString();
  }

  private Single<HttpClientResponse> send(String path, HttpMethod httpMethod, Object obj) {
    final var body = new JsonObject(Json.encode(obj));
    return null;
  }

  private Single<HttpClientResponse> sendMessage(SendMessage sendMessage) {

    final var body = new JsonObject(Json.encode(sendMessage));
    return httpService.send(HttpMethod.POST, url("sendMessage"), body);
  }

  private Single<HttpClientResponse> editMessageText(EditMessageText request) {

    final var body = new JsonObject(Json.encode(request));
    return httpService.send(HttpMethod.POST, url("editMessageText"), body);
  }

  private Single<HttpClientResponse> sendDocument(SendDocument sendDocument) {

    final var multipartForm = sendDocument.multipartForm();

    Optional.ofNullable(sendDocument.caption())
        .ifPresent(value -> multipartForm.attribute("caption", value));

    Optional.ofNullable(sendDocument.parseMode())
        .map(Enum::name)
        .ifPresent(value -> multipartForm.attribute("parse_mode", value));

    final var request = HttpClientRequest.builder()
        .url(url("sendDocument"))
        .httpMethod(HttpMethod.POST)
        .multipartForm(multipartForm
            .attribute("chat_id", String.valueOf(sendDocument.chatId()))
        )
        .build();

    return httpService.send(request);
  }

  private Single<HttpClientResponse> getWebhookInfo() {
    return httpService.get(url("getWebhookInfo"));
  }

  private Single<HttpClientResponse> deleteWebhook() {
    return httpService.delete(url("deleteWebhook"));
  }

  private Single<HttpClientResponse> setWebhook(JsonObject jsonObject) {
    return httpService.send(HttpMethod.POST, url("setWebhook"), jsonObject);
  }

  private Single<HttpClientResponse> setDefaultWebhook() {
    return vertxHandler.get(SET_WEBHOOK, config().getJsonObject("default_webhook_config"));
  }
}
