package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Completable;
import io.vertx.ext.web.multipart.MultipartForm;
import java.io.File;
import kyo.yaz.condominium.manager.core.util.ZipUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendLogs {

  private final TelegramRestApi restApi;


  public Completable sendLogs(long chatId, String caption) {
    return Completable.defer(() -> {

      final var dest = "logs.zip";
      ZipUtility.zipDirectory(new File("log"), dest);
      return restApi.sendDocument(chatId, caption, MultipartForm.create()
              .binaryFileUpload("document",
                  dest,
                  dest,
                  MediaType.TEXT_PLAIN_VALUE))
          .ignoreElement()
          //.doOnComplete(() -> Files.delete(Paths.get(dest)))
          ;
    });
  }
}
