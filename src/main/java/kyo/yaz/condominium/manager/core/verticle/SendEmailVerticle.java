package kyo.yaz.condominium.manager.core.verticle;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import java.io.File;
import java.io.StringReader;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import kyo.yaz.condominium.manager.core.domain.SendEmailRequest;
import kyo.yaz.condominium.manager.core.provider.GmailProvider;
import kyo.yaz.condominium.manager.core.util.GmailUtil;
import kyo.yaz.condominium.manager.core.vertx.VertxUtil;
import kyo.yaz.condominium.manager.persistence.entity.EmailConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class SendEmailVerticle extends BaseVerticle {

  public static final String SEND = "send-email";
  public static final String CHECK_EMAIL_CONFIG = "check-email-config";
  public static final String CLEAR = "clear-email-config";
  private static final Map<String, Gmail> map = new HashMap<>();
  private final HttpTransport transport;
  private final JsonFactory jsonFactory;

  @Override
  public void start() throws Exception {
    eventBusConsumer(SEND, this::send);
    vertx.eventBus().consumer(CLEAR, m -> {
      map.clear();
      m.reply(true);
    });
    eventBusConsumer(CHECK_EMAIL_CONFIG, this::checkEmailConfig);
  }

  private Single<Boolean> checkEmailConfig(EmailConfig emailConfig) {

    return gmailSingle(emailConfig)
        .map(gmail -> {
          final var labelList = gmail.users().labels().list("me");
          final var httpRequest = labelList.buildHttpRequest();

          return httpRequest.executeAsync();
        })
        .flatMap(Single::fromFuture)
        .map(response -> {
          final var statusCode = response.getStatusCode();
          final var body = response.parseAsString();

          // log.info("code {} body {}", statusCode, body);

          return statusCode == 200;
        })
        .retry(3);
  }

  private Single<Gmail> gmailSingle(EmailConfig emailConfig) {
    final var gmail = map.get(emailConfig.id());

    if (gmail != null) {
      log.info("Gmail config found for {}", emailConfig.id());
      return Single.just(gmail);
    }

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

          return gmailProvider.gmail();
        }));
  }


  private Single<String> send(SendEmailRequest request) {

    return gmailSingle(request.emailConfig())
        .map(gmail -> {

          final var gMessage = GmailUtil.fromBase64(request.message());
          final var send = gmail.users().messages().send("me", gMessage);

          return vertx.<String>executeBlocking(promise -> {
            try {
              final var execute = send.execute();
              final var string = execute.toString();
              log.info("MSG_SENT: {}", string);
              promise.complete(string);

            } catch (Exception e) {
              log.error("ERROR_SENDING_EMAIL", e);
              promise.fail(e);
            }
          });


        })
        .flatMap(vertxHandler()::single);
  }
}
