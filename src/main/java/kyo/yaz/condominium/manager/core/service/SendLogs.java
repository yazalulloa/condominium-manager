package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Completable;
import java.nio.file.Files;
import java.nio.file.Paths;
import kyo.yaz.condominium.manager.core.service.telegram.TelegramRestApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendLogs {

  private final TelegramRestApi restApi;
  private final LogService logService;


  public Completable sendLogs(long chatId, String caption) {
    return Completable.defer(() -> {

      final var dest = logService.zipLogs();
      return restApi.sendDocument(chatId, caption, dest, dest, MediaType.TEXT_PLAIN_VALUE)
          .ignoreElement()
          .doOnComplete(() -> Files.delete(Paths.get(dest)));
    });
  }
}
