package kyo.yaz.condominium.manager.core.config;

import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Slf4j
@Lazy
@Configuration
@ConfigurationPropertiesScan
public class GoogleConfig {

    @Bean
    public JsonFactory providesJsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

    @Bean
    public HttpTransport providesHttpTransport() {
        try {
            return GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public GooglePublicKeysManager providesGooglePublicKeysManager(HttpTransport transport, JsonFactory jsonFactory) {
        return new GooglePublicKeysManager(transport, jsonFactory);
    }

}
