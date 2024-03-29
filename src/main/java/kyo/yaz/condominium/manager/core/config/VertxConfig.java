package kyo.yaz.condominium.manager.core.config;

import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import kyo.yaz.condominium.manager.core.service.HttpServiceImpl;
import kyo.yaz.condominium.manager.core.service.VertxHttpService;
import kyo.yaz.condominium.manager.core.verticle.HttpClientVerticle;
import kyo.yaz.condominium.manager.core.verticle.ProcessLoggedUserVerticle;
import kyo.yaz.condominium.manager.core.verticle.SendEmailVerticle;
import kyo.yaz.condominium.manager.core.verticle.TelegramVerticle;
import kyo.yaz.condominium.manager.core.vertx.VerticleConfigDeployer;
import kyo.yaz.condominium.manager.core.vertx.VertxHandler;
import kyo.yaz.condominium.manager.core.vertx.VertxHandlerImpl;
import kyo.yaz.condominium.manager.core.vertx.codecs.DefaultJacksonMessageCodec;
import kyo.yaz.condominium.manager.core.vertx.domain.VerticleRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class VertxConfig {

  @Bean
  public Vertx vertx(@Value("${app.vertx.verticles_config_path}") String verticlesConfigPath,
      VerticleFactory verticleFactory) {
    final var options = new VertxOptions();
    final var loopPoolSize = options.getEventLoopPoolSize();

    log.info("CORES {}", loopPoolSize);

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

    final var verticlePrefix = verticleFactory.prefix();
    Stream.of(ProcessLoggedUserVerticle.class)
        .map(Class::getName)
        .map(name -> STR."\{verticlePrefix}:\{name}")
        .forEach(verticle -> vertx.deployVerticle(verticle, new DeploymentOptions().setInstances(loopPoolSize)));

    final var verticleRecords = Set.of(
        new VerticleRecord("telegram_config", STR."\{verticlePrefix}:\{TelegramVerticle.class.getName()}"),
       // new VerticleRecord("http_client_options", STR."\{verticlePrefix}:\{HttpClientVerticle.class.getName()}"),
        new VerticleRecord("send_email_config", STR."\{verticlePrefix}:\{SendEmailVerticle.class.getName()}")
    );

    final var deployer = VerticleConfigDeployer.builder()
        .vertx(vertx)
        .verticleInstances(loopPoolSize)
        .loadConfigDelayTime(10)
        .loadConfigDelayTimeUnit(TimeUnit.SECONDS)
        .verticles(new HashSet<>(verticleRecords))
        .configRetrieverOptions(configRetrieverOptions)
        .build();

    deployer.loadConfig()
        .onSuccess((f) -> log.info("Successfully deployed verticles"))
        .onFailure(t -> log.error("ERROR DEPLOYING VERTICLE", t));

    return vertx;
  }

  @Bean
  public io.vertx.rxjava3.core.Vertx vertxRx(Vertx vertx) {
    return io.vertx.rxjava3.core.Vertx.newInstance(vertx);
  }

  @Bean
  public EventBus eventBus(Vertx vertx) {
    return vertx.eventBus();
  }

  @Bean
  public VertxHandlerImpl vertxHandler(Vertx vertx) {
    return new VertxHandlerImpl(vertx);
  }

  @Bean
  public HttpServiceImpl httpService(Vertx vertx, VertxHandler vertxHandler) {
    final var config = new JsonObject();
    final var webClient = WebClient.create(vertx, new WebClientOptions(config));

    final var trustAllWebClientOptions = new WebClientOptions()
        .setTrustAll(true)
        .setVerifyHost(false);

    final var trustAll = WebClient.create(vertx, trustAllWebClientOptions);

    return new HttpServiceImpl(vertxHandler, new VertxHttpService(webClient, config),
        new VertxHttpService(trustAll, config));
  }
}
