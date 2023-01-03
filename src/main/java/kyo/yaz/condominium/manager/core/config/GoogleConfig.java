package kyo.yaz.condominium.manager.core.config;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import kyo.yaz.condominium.manager.core.config.domain.GmailConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
@Lazy
@Configuration
@ConfigurationPropertiesScan
public class GoogleConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.gmail")
    public GmailConfig gmailConfig() {
        return new GmailConfig();
    }

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

    @Bean
    public Gmail gmail(HttpTransport transport, JsonFactory jsonFactory,
                       GmailConfig gmailConfig) {
        try {

            List<String> SCOPES = List.of(GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_SEND);
            final var inputStream = new FileInputStream(gmailConfig.getCredentialsPath());
            final var googleClientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(inputStream));
            final var googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(transport, jsonFactory, googleClientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(gmailConfig.getTokensPath())))
                    .setAccessType("offline")
                    .build();

            final var localServerReceiver = new LocalServerReceiver.Builder().setPort(gmailConfig.getPort()).build();

            final var credential = new AuthorizationCodeInstalledApp(googleAuthorizationCodeFlow, localServerReceiver).authorize("user");

            return new Gmail.Builder(transport, jsonFactory, credential)
                    .setApplicationName(gmailConfig.getAppName())
                    .build();

        } catch (Exception e) {
            log.error("ERROR gmail", e);
            throw new RuntimeException(e);
        }
    }
}
