package kyo.yaz.condominium.manager.core.provider;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import lombok.Builder;
import lombok.ToString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Builder(toBuilder = true)
@ToString
public class GmailProvider {
    private final String appName;
    private final int port;
    private final String credentialsPath;
    private final String tokensPath;

    private final HttpTransport transport;
    private final JsonFactory jsonFactory;


    public Gmail gmail() throws IOException {
        List<String> SCOPES = List.of(GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_SEND);
        final var inputStream = new FileInputStream(credentialsPath);
        final var googleClientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(inputStream));
        final var googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(transport, jsonFactory, googleClientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(tokensPath)))
                .setAccessType("offline")
                .build();

        final var localServerReceiver = new LocalServerReceiver.Builder().setPort(port).build();

        final var authorize = new AuthorizationCodeInstalledApp(googleAuthorizationCodeFlow, localServerReceiver).authorize("user");

        return new Gmail.Builder(transport, jsonFactory, authorize)
                .setApplicationName(appName)
                .build();
    }

}
