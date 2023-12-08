package kyo.yaz.condominium.manager.core.service.telegram;

import io.reactivex.rxjava3.core.Completable;
import java.util.LinkedList;
import java.util.List;
import kyo.yaz.condominium.manager.core.domain.telegram.InlineKeyboardButton;
import kyo.yaz.condominium.manager.core.domain.telegram.InlineKeyboardMarkup;
import kyo.yaz.condominium.manager.core.domain.telegram.SendMessage;
import kyo.yaz.condominium.manager.core.service.BackupService;
import kyo.yaz.condominium.manager.core.service.DeleteDirAfterDelay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelegramSendEntityBackups {

  public static final String CALLBACK_KEY = "/backup_";

  private static final String ALL_KEY = "ALL";

  private final TelegramRestApi restApi;
  private final DeleteDirAfterDelay deleteDirAfterDelay;
  private final BackupService backupService;


  public Completable sendAvailableBackups(long chatId) {
    final List<List<InlineKeyboardButton>> list = new LinkedList<>();
    backupService.keys()
        .stream()
        .sorted()
        .map(key -> InlineKeyboardButton.callbackData(key, CALLBACK_KEY + key))
        .map(List::of)
        .forEach(list::add);
    list.add(List.of(InlineKeyboardButton.callbackData("ALL", CALLBACK_KEY + ALL_KEY)));

    final var keyboardMarkup = InlineKeyboardMarkup.builder()
        .inlineKeyboard(list)
        .build();

    final var sendMessage = SendMessage.builder()
        .chatId(chatId)
        .text("Escoge una opcion")
        .replyMarkup(keyboardMarkup)
        .build();

    return restApi.sendMessage(sendMessage)
        .ignoreElement();
  }

  public Completable resolve(long chatId, String text) {

    if (text.equals(ALL_KEY)) {

      return backupService.allGz()
          .flatMap(pair -> restApi.sendDocument(chatId, "all", pair.getFirst(), pair.getSecond(), "application/gzip")
              .doAfterTerminate(deleteDirAfterDelay::deleteTmp))
          .ignoreElement();

    }

    final var downloader = backupService.get(text);
    if (downloader == null) {
      return restApi.sendMessage(chatId, "DOWNLOADER NOT FOUND FOR " + text)
          .ignoreElement();
    }

    return downloader.download()
        .flatMap(fileResponse -> restApi.sendDocument(chatId, fileResponse.fileName(), fileResponse)
            .doAfterTerminate(() -> deleteDirAfterDelay.deleteDirNow(fileResponse.path())))
        .ignoreElement();
  }
}
