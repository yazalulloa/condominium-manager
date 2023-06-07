package kyo.yaz.condominium.manager.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.jackson.DatabindCodec;
import kyo.yaz.condominium.manager.core.util.JacksonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    final var mapper = JacksonUtil.defaultConfig(DatabindCodec.mapper());
    JacksonUtil.defaultConfig(DatabindCodec.prettyMapper());
    return mapper;
  }
}
