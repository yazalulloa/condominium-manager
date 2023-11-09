import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import kyo.yaz.condominium.manager.core.provider.GmailProvider;
import kyo.yaz.condominium.manager.core.util.JacksonUtil;
import org.junit.jupiter.api.Test;

public class GmailConfigTest {

  @Test
  void buildConfig() throws IOException, GeneralSecurityException {
//    String key = "marlene";
    String key = "kyotaidoshin";

    final var yamlMapper = JacksonUtil.yamlMapper();

    final var sendEmailConfig = yamlMapper.readTree(new File("config/verticles.yml"))
        .get("send_email_config");

    final var jsonFactory = GsonFactory.getDefaultInstance();
    final var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    final var gmailProvider = GmailProvider.builder()
        .appName(sendEmailConfig.get("app_name").textValue())
        .port(sendEmailConfig.get("port").intValue())
        .clientSecretsReader(new FileReader("config/gmail/" + key + "-credentials.json"))
        .tokenDataStore(new FileDataStoreFactory(new File("gmail/" + key)))
        .transport(httpTransport)
        .jsonFactory(jsonFactory)
        .build();

    final var gmail = gmailProvider.gmail();

    gmail.users().labels().list("me").execute().getLabels().forEach(System.out::println);
  }

}
