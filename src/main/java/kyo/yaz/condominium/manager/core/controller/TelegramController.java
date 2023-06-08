package kyo.yaz.condominium.manager.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.core.Vertx;
import java.util.concurrent.CompletableFuture;
import kyo.yaz.condominium.manager.core.verticle.TelegramVerticle;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(TelegramController.PATH)
@RequiredArgsConstructor
public class TelegramController {

  public static final String PATH = "/0570b232-ab43-4242-8a9e-d5f035ef7580";

  private final Vertx vertx;

  @Async
  @PostMapping(path = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<String> webhook(@RequestBody JsonNode json) {
    vertx.eventBus().sender(TelegramVerticle.ADDRESS).write(json);
    return CompletableFuture.completedFuture("ok");
  }
}
