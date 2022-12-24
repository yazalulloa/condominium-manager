package kyo.yaz.condominium.manager.core.config;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.spi.VerticleFactory;
import kyo.yaz.condominium.manager.core.verticle.HttpClientVerticle;
import kyo.yaz.condominium.manager.core.vertx.codecs.DefaultJacksonMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Configuration
public class VertxDeployer {


    @Bean
    public Vertx vertx(VerticleFactory verticleFactory) {
        log.info("VERTX BEAN");

        return deployVertx(verticleFactory);
    }

    @Bean
    public EventBus eventBus(Vertx vertx) {
        log.info("EventBus BEAN");

        return configureEventBus(vertx);
    }

    private Vertx deployVertx(VerticleFactory verticleFactory) {
        final var options = new VertxOptions();

        final var vertx = Vertx.vertx(options);

        vertx.registerVerticleFactory(verticleFactory);

        final var classes = Set.of(HttpClientVerticle.class);

        final var deploymentOptions = new DeploymentOptions().setInstances(options.getEventLoopPoolSize());
        classes.stream().map(Class::getName)
                .map(clazz -> verticleFactory.prefix() + ":" + clazz)
                .forEach(verticleName -> vertx.deployVerticle(verticleName, deploymentOptions));

        return vertx;
    }

    private EventBus configureEventBus(Vertx vertx) {
        //final var vertx = vertxCachedResultSupplier.get();
        final var eventBus = vertx.eventBus();
        final var defaultJacksonMessageCodec = new DefaultJacksonMessageCodec();
        eventBus.registerCodec(defaultJacksonMessageCodec);
        eventBus.codecSelector(body -> defaultJacksonMessageCodec.name());
        return eventBus;
    }
}
