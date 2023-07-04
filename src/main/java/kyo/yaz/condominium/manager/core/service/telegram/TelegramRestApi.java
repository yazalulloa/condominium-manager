package kyo.yaz.condominium.manager.core.service.telegram;

import io.reactivex.rxjava3.core.Single;
import io.vertx.ext.web.multipart.MultipartForm;
import java.util.Objects;
import kyo.yaz.condominium.manager.core.domain.FileResponse;
import kyo.yaz.condominium.manager.core.domain.HttpClientResponse;
import kyo.yaz.condominium.manager.core.domain.telegram.EditMessageText;
import kyo.yaz.condominium.manager.core.domain.telegram.ParseMode;
import kyo.yaz.condominium.manager.core.domain.telegram.SendDocument;
import kyo.yaz.condominium.manager.core.domain.telegram.SendMessage;
import kyo.yaz.condominium.manager.core.verticle.TelegramVerticle;
import kyo.yaz.condominium.manager.core.vertx.VertxHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelegramRestApi {

  private final VertxHandler vertxHandler;


  public Single<HttpClientResponse> request(SendMessage sendMessage) {

    final var build = sendMessage.toBuilder()
        .parseMode(Objects.requireNonNullElse(sendMessage.parseMode(), ParseMode.HTML))
        .build();

    return vertxHandler.get(TelegramVerticle.SEND_MESSAGE, build);
  }

  public Single<HttpClientResponse> sendMessage(long chatId, String text) {

    final var sendMessage = SendMessage.builder()
        .chatId(chatId)
        .text(text)
        .build();

    return request(sendMessage);
  }

  public Single<HttpClientResponse> sendMessage(SendMessage sendMessage) {
    return request(sendMessage);
  }

  public Single<HttpClientResponse> editMessageText(EditMessageText editMessageText) {
    return vertxHandler.get(TelegramVerticle.EDIT_MESSAGE_TEXT, editMessageText);

      /*  return HttpUtil.single(this.editMessageText, webTarget -> webTarget.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(editMessageText)));*/
  }

  public Single<HttpClientResponse> getWebhookInfo() {
    return vertxHandler.get(TelegramVerticle.GET_WEBHOOK_INFO);
  }

  public Single<HttpClientResponse> deleteWebhook() {
    return vertxHandler.get(TelegramVerticle.DELETE_WEBHOOK);
  }

  public Single<HttpClientResponse> setDefaultWebhook() {
    return vertxHandler.get(TelegramVerticle.SET_DEFAULT_WEBHOOK);
  }

  public Single<HttpClientResponse> sendDocument(long chatId, String caption, FileResponse fileResponse) {
    return sendDocument(chatId, caption, fileResponse.fileName(), fileResponse.path(), fileResponse.contentType());
  }

  public Single<HttpClientResponse> sendDocument(long chatId, String caption, String fileName, String path,
      String mediaType) {

    final var multipartForm = MultipartForm.create()
        .binaryFileUpload("document",
            fileName,
            path,
            mediaType);

    return sendDocument(chatId, caption, multipartForm);
  }

  public Single<HttpClientResponse> sendDocument(long chatId, String caption, MultipartForm multipartForm) {
    final var sendDocument = SendDocument.builder()
        .chatId(chatId)
        .caption(caption)
        .multipartForm(multipartForm)
        .build();

    return sendDocument(sendDocument);
  }

  public Single<HttpClientResponse> sendDocument(SendDocument sendDocument) {
    return vertxHandler.get(TelegramVerticle.SEND_DOCUMENT, sendDocument);

  }


}
