package kyo.yaz.condominium.manager.core.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.jackson.DatabindCodec;
import kyo.yaz.condominium.manager.core.util.JacksonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
public class JacksonComponent {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JacksonUtil.defaultConfig(DatabindCodec.mapper());
    }
}
