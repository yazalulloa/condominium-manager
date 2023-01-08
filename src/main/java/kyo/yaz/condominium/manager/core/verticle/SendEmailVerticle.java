package kyo.yaz.condominium.manager.core.verticle;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import kyo.yaz.condominium.manager.core.domain.ReceiptEmailFrom;
import kyo.yaz.condominium.manager.core.domain.SendEmailRequest;
import kyo.yaz.condominium.manager.core.provider.GmailProvider;
import kyo.yaz.condominium.manager.core.util.GmailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class SendEmailVerticle extends AbstractVerticle {

    public static final String SEND = "send-email";
    private static final Map<ReceiptEmailFrom, Gmail> map = new HashMap<>();
    private final HttpTransport transport;
    private final JsonFactory jsonFactory;

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(SEND, this::send);
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
                .credentialsPath(jsonObject.getString("credentials_path"))
                .tokensPath(jsonObject.getString("tokens_path"))
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


            final var send = gmail.users().messages().send("me", gMessage);
            final var execute = send.execute();
            final var string = execute.toString();

            message.reply(string);
        } catch (Exception e) {
            message.reply(e);
        }
    }
}
