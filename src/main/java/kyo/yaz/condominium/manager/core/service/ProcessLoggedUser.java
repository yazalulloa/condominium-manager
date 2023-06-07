package kyo.yaz.condominium.manager.core.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import kyo.yaz.condominium.manager.core.verticle.ProcessLoggedUserVerticle;
import kyo.yaz.condominium.manager.persistence.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProcessLoggedUser {

  private final Vertx vertx;

  @Autowired
  public ProcessLoggedUser(Vertx vertx) {
    this.vertx = vertx;
  }

  public void process(User user) {
    log.info("USER {}", Json.encodePrettily(user));
    vertx.eventBus().sender(ProcessLoggedUserVerticle.ADDRESS).write(user);
  }
}
