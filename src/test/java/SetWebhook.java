import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

public class SetWebhook {

  public static void main(String[] args) {
    final var jsonObject = new JsonObject()
        .put("url", "https://condominium-manager.onrender.com/0570b232-ab43-4242-8a9e-d5f035ef7580/webhook");

    final var vertx = Vertx.vertx();
    final var client = WebClient.create(vertx);

    client.postAbs("https://api.telegram.org/bot5636994232:AAHFStWULB4P-JQ6ax_vbHYKytyXKYM94fk/setWebhook")
        .sendJsonObject(jsonObject)
        .onSuccess(response -> {
          System.out.println(response.statusCode());
          System.out.println(response.body().toString());
        })
        .onFailure(Throwable::printStackTrace);

  }

}
