package kyo.yaz.condominium.manager.core.vertx;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import kyo.yaz.condominium.manager.core.vertx.domain.VerticleRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Builder
@AllArgsConstructor
public class VerticleConfigDeployer {

    private final Vertx vertx;
    private final int verticleInstances;
    private final long loadConfigDelayTime;
    private final TimeUnit loadConfigDelayTimeUnit;
    private final Set<VerticleRecord> verticles;
    private final ConfigRetrieverOptions configRetrieverOptions;


    public Future<CompositeFuture> loadConfig() {
        /*
        final var fileStore = new ConfigStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject()
                        .put("path", "config.yml")
                );

        final var configRetrieverOptions = new ConfigRetrieverOptions()
                .addStore(fileStore)
                .setScanPeriod(5000);
        */

        final var retriever = ConfigRetriever.create(vertx, configRetrieverOptions);

        vertx.setTimer(loadConfigDelayTimeUnit.toMillis(loadConfigDelayTime), l -> {
            retriever.configStream()
                    .handler(this::deployVerticles);
        });

        return retriever.getConfig()
                .flatMap(this::deployVerticles);
    }

    private CompositeFuture deployVerticles(JsonObject config) {

        final List<Future> futures = verticles.stream()
                .map(verticleRecord -> {
                    final var jsonObject = config.getJsonObject(verticleRecord.configKey());

                    if (jsonObject != null) {

                        final var shouldDeploy = verticleRecord.oldConfig() == null || !verticleRecord.oldConfig().equals(jsonObject);

                        if (shouldDeploy) {

                            final var oldVerticleId = verticleRecord.verticleId();

                            return vertx.deployVerticle(verticleRecord.verticle(), new DeploymentOptions().setInstances(verticleInstances).setConfig(jsonObject))
                                    .onSuccess(verticleId -> {

                                        final var build = verticleRecord.toBuilder()
                                                .verticleId(verticleId)
                                                .oldConfig(jsonObject)
                                                .build();

                                        verticles.remove(build);
                                        verticles.add(build);

                                        if (oldVerticleId != null) {
                                            log.info("UNDEPLOY {} {}", verticleRecord.configKey(), oldVerticleId);
                                            vertx.undeploy(oldVerticleId);
                                        }
                                    });
                        }
                    }

                    return Future.succeededFuture();
                })
                .collect(Collectors.toList());

        return CompositeFuture.all(futures);
    }
}
