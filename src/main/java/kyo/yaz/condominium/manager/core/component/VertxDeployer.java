package kyo.yaz.condominium.manager.core.component;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.spi.VerticleFactory;
import kyo.yaz.condominium.manager.core.util.CachedResultSupplier;
import kyo.yaz.condominium.manager.core.verticle.HttpClientVerticle;
import kyo.yaz.condominium.manager.core.vertx.codecs.DefaultJacksonMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.awt.image.DataBuffer;
import java.util.Set;

@Slf4j
@Component
public class VertxDeployer {


    private final CachedResultSupplier<Vertx> vertxCachedResultSupplier;
    private final CachedResultSupplier<EventBus> eventBusCachedResultSupplier;

    private final VerticleFactory verticleFactory;

    public VertxDeployer(VerticleFactory verticleFactory) {
        log.info("VertxDeployer initialized");
        this.verticleFactory = verticleFactory;
        this.vertxCachedResultSupplier = new CachedResultSupplier<>(this::deployVertx);
        this.eventBusCachedResultSupplier = new CachedResultSupplier<>(this::configureEventBus);



    }

    private Vertx deployVertx() {
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

    private EventBus configureEventBus() {
        final var vertx = vertxCachedResultSupplier.get();
        final var eventBus = vertx.eventBus();
        final var defaultJacksonMessageCodec = new DefaultJacksonMessageCodec();
        eventBus.registerCodec(defaultJacksonMessageCodec);
        eventBus.codecSelector(body -> defaultJacksonMessageCodec.name());
        return eventBus;
    }


    @Bean
    public Vertx vertx() {
        return vertxCachedResultSupplier.get();
    }

    @Bean
    public EventBus eventBus() {
        return eventBusCachedResultSupplier.get();
    }
}
