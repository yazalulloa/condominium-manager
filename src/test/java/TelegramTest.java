import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import kyo.yaz.condominium.manager.core.domain.HttpClientRequest;
import kyo.yaz.condominium.manager.core.service.HttpServiceImpl;
import kyo.yaz.condominium.manager.core.util.JacksonUtil;
import kyo.yaz.condominium.manager.core.verticle.HttpClientVerticle;
import kyo.yaz.condominium.manager.core.vertx.VertxHandlerImpl;
import kyo.yaz.condominium.manager.core.vertx.codecs.DefaultJacksonMessageCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;

@ExtendWith(VertxExtension.class)
public class TelegramTest {

  @BeforeEach
  @DisplayName("Deploy a verticle")
  void prepare(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new HttpClientVerticle(), testContext.succeedingThenComplete());

    final var eventBus = vertx.eventBus();
    final var defaultJacksonMessageCodec = new DefaultJacksonMessageCodec();
    eventBus.registerCodec(defaultJacksonMessageCodec);
    eventBus.codecSelector(body -> defaultJacksonMessageCodec.name());
  }

  @Test
  public void setWebhook() throws IOException {

    final var jsonNode = jsonNode().get("telegram_config");

    final var jsonObject = new JsonObject()
        .put("url", jsonNode.get("default_webhook_config").get("url").textValue());

    final var vertx = Vertx.vertx();
    final var client = WebClient.create(vertx);

    final var url = jsonNode.get("url").textValue();
    System.out.println(url);

    client.postAbs(url + "setWebhook")
        .sendJsonObject(jsonObject)
        .onSuccess(response -> {
          System.out.println(response.statusCode());
          System.out.println(response.body().toString());
        })
        .onFailure(Throwable::printStackTrace);

  }

  @Test
  void http(Vertx vertx, VertxTestContext testContext) throws Throwable {
    final var jsonNode = jsonNode();
    final var url = jsonNode.get("telegram_config").get("url").textValue();
    System.out.println(url);

    final var chatId = jsonNode("config/application.yml").get("app")
        .get("notification_chat_id")
        .asText();
    final var dest = "logs.zip";

    MultipartForm form = MultipartForm.create()
        .attribute("chat_id", chatId)
        .attribute("caption", "test")
        .binaryFileUpload("document",
            dest,
            dest,
            MediaType.TEXT_PLAIN_VALUE)
        .binaryFileUpload(
            "document",
            "pom.xml",
            "pom.xml",
            MediaType.APPLICATION_XML_VALUE)
        ;

    final HttpClientRequest request = HttpClientRequest.builder()
        .url(url + "sendDocument")
        .httpMethod(HttpMethod.POST)
        .body(new JsonObject().put("test", "test"))
        .multipartForm(form)
        .build();

    final var service = new HttpServiceImpl(new VertxHandlerImpl(vertx));

    service.send(request)
        .ignoreElement()
        .subscribe(testContext::completeNow, testContext::failNow);

    testContext.awaitCompletion(10, TimeUnit.SECONDS);
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }

  @Test
  void sendFile(Vertx vertx, VertxTestContext testContext) throws Throwable {
    final var jsonNode = jsonNode();

    final var url = jsonNode.get("telegram_config").get("url").textValue();
    System.out.println(url);

    final var chatId = jsonNode("config/application.yml").get("app")
        .get("notification_chat_id")
        .asText();

    MultipartForm form = MultipartForm.create()
        .attribute("chat_id", chatId)
        .attribute("caption", "test")
        .binaryFileUpload(
            "document",
            "pom.xml",
            "pom.xml",
            MediaType.APPLICATION_XML_VALUE);

    final var client = WebClient.create(vertx);

    client.postAbs(url + "sendDocument")
        .sendMultipartForm(form)
        .onComplete(ar -> {
          if (ar.failed()) {
            ar.cause().printStackTrace();
          } else {
            final var response = ar.result();
            System.out.println(response.statusCode());
            System.out.println(response.body().toString());
          }

          testContext.completeNow();
        });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
    if (testContext.failed()) {
      throw testContext.causeOfFailure();
    }
  }

  private JsonNode jsonNode() throws IOException {
    return jsonNode("config/verticles.yml");
  }

  private JsonNode jsonNode(String path) throws IOException {
    final var mapper = JacksonUtil.yamlMapper();

    return mapper.readTree(new File(path))
        ;
  }


}
