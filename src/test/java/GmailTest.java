import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import jakarta.mail.MessagingException;
import kyo.yaz.condominium.manager.core.domain.EmailRequest;
import kyo.yaz.condominium.manager.core.provider.GmailProvider;
import kyo.yaz.condominium.manager.core.util.GmailUtil;
import kyo.yaz.condominium.manager.core.util.MimeMessageUtil;
import kyo.yaz.condominium.manager.core.util.RandomUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

public class GmailTest {

    @Test
    void test() throws IOException, GeneralSecurityException, MessagingException {



        final var gmailProvider = GmailProvider.builder()
                .appName("CONDOMINIO")
                .port(38123)
                .credentialsPath("config/gmail/kyotaidoshin.json")
                .tokensPath("config/gmail/tokens/kyotaidoshin")
               // .credentialsPath("config/gmail/marlene.json")
               // .tokensPath("config/gmail/tokens/marlene")
                .jsonFactory(GsonFactory.getDefaultInstance())
                .transport(GoogleNetHttpTransport.newTrustedTransport())
                .build();

        final var gmail = gmailProvider.gmail();

        final var profile = gmail.users().getProfile("me");
        System.out.println(profile.getUserId());
        System.out.println(profile.getAccessToken());

        final var request = EmailRequest.builder()
                .from("kyotaidoshin@gmail.com")
               // .from("rodriguezulloa15@gmail.com")
                .to(Set.of("yzlup2@gmail.com"))
                .cc(Set.of("yzlup2@gmail.com"))
                .bcc(Set.of("yzlup2@gmail.com"))
                .subject("Test %s".formatted(RandomUtil.randomIntStr(8)))
                .text("Test %s".formatted(RandomUtil.randomIntStr(8)))
                .build();

        final var gMessage = GmailUtil.fromBase64(GmailUtil.mimeMessageToBase64(MimeMessageUtil.createEmail(request)));
        final var labelList = gmail.users().labels().list("me");
        final var httpResponse = labelList.executeUnparsed();
        System.out.println(httpResponse.getStatusCode());
        System.out.println(httpResponse.parseAsString());
        final var labels = labelList.execute();

        System.out.println(labels.getLabels());


        final var send = gmail.users().messages().send("me", gMessage);
        System.out.println(send.getUserId());
        final var execute = send.execute();
        System.out.println(execute.toString());
    }
}
