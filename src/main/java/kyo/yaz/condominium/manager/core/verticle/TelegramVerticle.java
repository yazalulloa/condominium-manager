package kyo.yaz.condominium.manager.core.verticle;

import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import kyo.yaz.condominium.manager.core.service.entity.TelegramChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
public class TelegramVerticle extends AbstractVerticle {

  public static final String ADDRESS = "web-hook";


  private final TelegramChatService service;

  @Override
  public void start() throws Exception {
    super.start();
    vertx.eventBus().<JsonNode>consumer(ADDRESS, m -> {
      final var json = m.body();
      log.info("JSON {}", Json.encodePrettily(json));
    });
  }
}
