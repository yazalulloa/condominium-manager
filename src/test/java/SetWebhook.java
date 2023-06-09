import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import java.io.File;
import java.io.IOException;
import kyo.yaz.condominium.manager.core.util.JacksonUtil;

public class SetWebhook {

  public static void main(String[] args) throws IOException {

    final var mapper = JacksonUtil.yamlMapper();

    final var jsonNode = mapper.readTree(new File("config/verticles.yml"))
        .get("telegram_config");

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

}
