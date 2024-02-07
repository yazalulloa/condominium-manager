package kyo.yaz.condominium.manager.core.service;

import io.vertx.core.json.jackson.DatabindCodec;
import org.junit.jupiter.api.Test;

public class CheckForJacksonModules {

  @Test
  void check() {
    DatabindCodec.mapper().getRegisteredModuleIds().forEach(System.out::println);
  }
}
