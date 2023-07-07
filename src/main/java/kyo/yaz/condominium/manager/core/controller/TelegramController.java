package kyo.yaz.condominium.manager.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.core.Vertx;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.core.verticle.TelegramVerticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(TelegramController.PATH)
@RequiredArgsConstructor
@Slf4j
public class TelegramController {

    public static final String PATH = "/0570b232-ab43-4242-8a9e-d5f035ef7580";

    private final Vertx vertx;
    private final TranslationProvider translationProvider;

    @Async
    @PostMapping(path = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<String> webhook(@RequestBody JsonNode json) {
        log.info("WEBHOOK {}", Thread.currentThread());
        vertx.eventBus().sender(TelegramVerticle.WEB_HOOK).write(json);
        return CompletableFuture.completedFuture("ok");
    }

    @Async
    @GetMapping(path = "/bundle")
    public CompletableFuture<String> bundle() {
        return CompletableFuture.completedFuture(translationProvider.printBundle());
    }
}
