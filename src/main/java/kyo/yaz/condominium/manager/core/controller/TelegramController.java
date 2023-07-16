package kyo.yaz.condominium.manager.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.vertx.core.Vertx;
import kyo.yaz.condominium.manager.core.domain.response.MetricResponse;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.core.verticle.TelegramVerticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(TelegramController.PATH)
@RequiredArgsConstructor
@Slf4j
public class TelegramController {

    public static final String PATH = "/0570b232-ab43-4242-8a9e-d5f035ef7580";

    private final Vertx vertx;
    private final TranslationProvider translationProvider;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;

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

    @Async
    @GetMapping(path = "/metrics", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<List<MetricResponse>> metrics() {

        final var metricResponses = meterRegistry.getMeters().stream()
                .map(meter -> {

                    final var id = meter.getId();

                    final var measurements = StreamSupport.stream(meter.measure().spliterator(), false)
                            .map(measurement -> new MetricResponse.Measurement(measurement.getStatistic().name(), measurement.getValue()))
                            .sorted(Comparator.comparing(MetricResponse.Measurement::statistic))
                            .toList();

                    final var tags = id.getTags().stream()
                            .map(tag -> new MetricResponse.Tags(tag.getKey(), tag.getValue()))
                            .sorted(Comparator.comparing(MetricResponse.Tags::key))
                            .toList();

                    return MetricResponse.builder()
                            .name(id.getName())
                            .type(id.getType().name())
                            .description(id.getDescription())
                            .baseUnit(id.getBaseUnit())
                            .measurements(measurements)
                            .tags(tags)
                            .build();
                })
                .sorted(Comparator.comparing(MetricResponse::type)
                        .thenComparing(MetricResponse::name))
                .toList();

        return CompletableFuture.completedFuture(metricResponses);
    }
}
