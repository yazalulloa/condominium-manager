package kyo.yaz.condominium.manager.core.config;

import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VerticleFactory;
import kyo.yaz.condominium.manager.core.verticle.HttpClientVerticle;
import kyo.yaz.condominium.manager.core.verticle.SendEmailVerticle;
import kyo.yaz.condominium.manager.core.vertx.VerticleConfigDeployer;
import kyo.yaz.condominium.manager.core.vertx.VertxHandlerImpl;
import kyo.yaz.condominium.manager.core.vertx.codecs.DefaultJacksonMessageCodec;
import kyo.yaz.condominium.manager.core.vertx.domain.VerticleRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class VertxConfig {

    @Bean
    public Vertx vertx(@Value("${app.vertx.verticles_config_path}") String verticlesConfigPath, VerticleFactory verticleFactory) {
        final var options = new VertxOptions();

        final var vertx = Vertx.vertx(options);

        vertx.registerVerticleFactory(verticleFactory);

        final var eventBus = vertx.eventBus();
        final var defaultJacksonMessageCodec = new DefaultJacksonMessageCodec();
        eventBus.registerCodec(defaultJacksonMessageCodec);
        eventBus.codecSelector(body -> defaultJacksonMessageCodec.name());

        final var fileStore = new ConfigStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject()
                        .put("path", verticlesConfigPath)
                );

        final var configRetrieverOptions = new ConfigRetrieverOptions()
                .addStore(fileStore)
                .setScanPeriod(5000);

        final var verticleRecords = Set.of(
                new VerticleRecord("http_client_options", verticleFactory.prefix() + ":" + HttpClientVerticle.class.getName()),
                new VerticleRecord("send_email_config", verticleFactory.prefix() + ":" + SendEmailVerticle.class.getName())
        );

        final var deployer = VerticleConfigDeployer.builder()
                .vertx(vertx)
                .verticleInstances(options.getEventLoopPoolSize())
                .loadConfigDelayTime(10)
                .loadConfigDelayTimeUnit(TimeUnit.SECONDS)
                .verticles(new HashSet<>(verticleRecords))
                .configRetrieverOptions(configRetrieverOptions)
                .build();

        deployer.loadConfig();

        return vertx;
    }

    @Bean
    public EventBus eventBus(Vertx vertx) {
        return vertx.eventBus();
    }

    @Bean
    public VertxHandlerImpl vertxHandler(Vertx vertx) {
        return new VertxHandlerImpl(vertx);
    }
}
