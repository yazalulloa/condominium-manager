package kyo.yaz.condominium.manager.core.service;

import io.vertx.core.Vertx;
import kyo.yaz.condominium.manager.core.verticle.ProcessLoggedUserVerticle;
import kyo.yaz.condominium.manager.persistence.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessLoggedUser {

  private final Vertx vertx;


  public void process(User user) {
    //log.info("USER {}", Json.encodePrettily(user));
    vertx.eventBus().sender(ProcessLoggedUserVerticle.ADDRESS).write(user);
  }
}
