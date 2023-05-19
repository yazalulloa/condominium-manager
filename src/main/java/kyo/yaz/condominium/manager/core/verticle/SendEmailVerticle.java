package kyo.yaz.condominium.manager.core.verticle;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import kyo.yaz.condominium.manager.core.domain.ReceiptEmailFrom;
import kyo.yaz.condominium.manager.core.domain.SendEmailRequest;
import kyo.yaz.condominium.manager.core.provider.GmailProvider;
import kyo.yaz.condominium.manager.core.util.GmailUtil;
import kyo.yaz.condominium.manager.core.vertx.Rx3Verticle;
import kyo.yaz.condominium.manager.core.vertx.VertxUtil;
import kyo.yaz.condominium.manager.persistence.entity.EmailConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
public class SendEmailVerticle extends Rx3Verticle {

    public static final String SEND = "send-email";
    public static final String CHECK_EMAIL_CONFIG = "check-email-config";
    private static final Map<ReceiptEmailFrom, Gmail> map = new HashMap<>();
    private final HttpTransport transport;
    private final JsonFactory jsonFactory;

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(SEND, this::send);
        eventBusConsumer(CHECK_EMAIL_CONFIG, this::checkEmailConfig);
    }

    private Single<Boolean> checkEmailConfig(EmailConfig emailConfig) {

        final var dir = "gmail/" + emailConfig.id();

        final var fileCreation = vertx.fileSystem().mkdirs(dir)
                .flatMap(v -> {
                    if (emailConfig.storedCredential() == null) {
                        return Future.succeededFuture();
                    }

                    final var filePath = dir + "/" + "StoredCredential";
                    final var bytes = Base64.getDecoder().decode(emailConfig.storedCredential());
                    return vertx.fileSystem().writeFile(filePath, Buffer.buffer(bytes));
                });

        return VertxUtil.completable(fileCreation)
                .andThen(Single.fromCallable(() -> {

                    final var gmailProvider = GmailProvider.builder()
                            .appName(config().getString("app_name"))
                            .port(config().getInteger("port"))
                            .clientSecretsReader(new StringReader(emailConfig.config()))
                            .tokenDataStore(new FileDataStoreFactory(new File(dir)))
                            .jsonFactory(jsonFactory)
                            .transport(transport)
                            .build();

                    final var gmail = gmailProvider.gmail();
                    final var labelList = gmail.users().labels().list("me");
                    final var httpRequest = labelList.buildHttpRequest();

                    return httpRequest.executeAsync();
                }))
                .flatMap(Single::fromFuture)
                .map(response -> {
                    final var statusCode = response.getStatusCode();
                    final var body = response.parseAsString();

                   // log.info("code {} body {}", statusCode, body);

                    return statusCode == 200;
                });


    }

    private Gmail gmail(ReceiptEmailFrom receiptEmailFrom) throws IOException {
        final var gmail = map.get(receiptEmailFrom);

        if (gmail != null) {
            return gmail;
        }

        final var jsonObject = config().getJsonObject(receiptEmailFrom.name());

        final var gmailProvider = GmailProvider.builder()
                .appName(config().getString("app_name"))
                .port(config().getInteger("port"))
                //.credentialsPath(jsonObject.getString("credentials_path"))
                // .tokensPath(jsonObject.getString("tokens_path"))
                .jsonFactory(jsonFactory)
                .transport(transport)
                .build();

        map.put(receiptEmailFrom, gmailProvider.gmail());

        return map.get(receiptEmailFrom);
    }

    private void send(Message<SendEmailRequest> message) {

        try {
            final var request = message.body();

            final var gMessage = GmailUtil.fromBase64(request.message());
            final var gmail = gmail(request.receiptEmailFrom());

            //log.info("SENDING {}", gMessage);

            final var send = gmail.users().messages().send("me", gMessage);
            final var execute = send.execute();
            final var string = execute.toString();

            log.info("MSG_SENT: {}", string);
            message.reply(string);
        } catch (Exception e) {

            log.error("ERROR_SENDING_EMAIL", e);
            message.reply(VertxUtil.replyException(e, getClass().toString()));
        }
    }
}
