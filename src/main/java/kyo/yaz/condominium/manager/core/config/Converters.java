package kyo.yaz.condominium.manager.core.config;

import java.util.Arrays;
import kyo.yaz.condominium.manager.persistence.repository.converter.ZonedDateTimeReadConverter;
import kyo.yaz.condominium.manager.persistence.repository.converter.ZonedDateTimeWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class Converters {

  @Bean
  public MongoCustomConversions mongoCustomConversions() {

    return new MongoCustomConversions(
        Arrays.asList(
            new ZonedDateTimeReadConverter(),
            new ZonedDateTimeWriteConverter()));
  }

}
