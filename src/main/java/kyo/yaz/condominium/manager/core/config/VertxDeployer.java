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

import java.util.Set;

@Slf4j
@Configuration
public class VertxDeployer {

    @Bean
    public Vertx vertx(VerticleFactory verticleFactory) {
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

    @Bean
    public EventBus eventBus(Vertx vertx) {
        final var eventBus = vertx.eventBus();
        final var defaultJacksonMessageCodec = new DefaultJacksonMessageCodec();
        eventBus.registerCodec(defaultJacksonMessageCodec);
        eventBus.codecSelector(body -> defaultJacksonMessageCodec.name());
        return eventBus;
    }
}
