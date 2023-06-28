package kyo.yaz.condominium.manager.core.service.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import java.util.stream.Collectors;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.domain.telegram.Chat;
import kyo.yaz.condominium.manager.core.domain.telegram.TelegramUpdate;
import kyo.yaz.condominium.manager.core.domain.telegram.TelegramUser;
import kyo.yaz.condominium.manager.core.service.SendLogs;
import kyo.yaz.condominium.manager.core.service.entity.RateService;
import kyo.yaz.condominium.manager.core.service.entity.TelegramChatService;
import kyo.yaz.condominium.manager.core.service.entity.UserService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.core.util.SystemUtil;
import kyo.yaz.condominium.manager.persistence.entity.TelegramChat;
import kyo.yaz.condominium.manager.persistence.entity.TelegramChat.TelegramChatId;
import kyo.yaz.condominium.manager.ui.views.telegram_chat.TelegramChatLinkHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelegramCommandResolver {

  private final TelegramChatService chatService;
  private final UserService userService;
  private final SendLogs sendLogs;
  private final TelegramRestApi telegramRestApi;
  private final RateService rateService;
  private final TelegramChatLinkHandler linkHandler;
  private final TelegramSendEntityBackups sendEntityBackups;


  public Completable resolve(JsonNode json) {
    return Completable.defer(() -> {
      final var update = DatabindCodec.mapper().treeToValue(json, TelegramUpdate.class);

      final var message = update.message();

      if (message != null) {
        final var text = message.text();
        final var from = message.from();
        final var chat = message.chat();

        if (text != null && from != null) {
          final var chatId = from.id();

          if (text.startsWith("/start") && text.length() > 8 && !from.isBot() && chat != null) {
            final var userId = text.substring(7).trim();

            return addAccount(userId, from, chat);
          }

          if (text.startsWith("/log")) {
            return sendLogs.sendLogs(chatId, "logs");
          }

          if (text.startsWith("/system_info")) {
            return telegramRestApi.sendMessage(chatId, SystemUtil.systemInfo().collect(Collectors.joining("\n")))
                .ignoreElement();
          }

          if (text.startsWith("/tasa")) {

            return rateService.last(Currency.USD, Currency.VED)
                .toSingle()
                .map(rate -> "%s - %s - %s - %s".formatted(rate.rate(), rate.dateOfRate(),
                    DateUtil.formatVe(rate.createdAt()), rate.id()))
                .flatMap(msg -> telegramRestApi.sendMessage(chatId, msg))
                .ignoreElement();
          }

          if (text.startsWith("/backups")) {
            return sendEntityBackups.sendAvailableBackups(chatId);
          }
        }
      }

      final var callbackQuery = update.callbackQuery();

      if (callbackQuery != null) {
        final var from = callbackQuery.from();
        if (callbackQuery.data().startsWith(TelegramSendEntityBackups.CALLBACK_KEY)) {
          return sendEntityBackups.resolve(from.id(),
              callbackQuery.data().replace(TelegramSendEntityBackups.CALLBACK_KEY, "").trim());
        }

      }

      return Completable.complete();
    });
  }

  private Completable addAccount(String userId, TelegramUser from, Chat chatToSave) {
    final var userMaybe = userService.find(userId)
        .cache();

    final var chatId = from.id();

    final var chatMaybe = chatService.find(userId, chatId)
        .cache();

    return Single.zip(userMaybe, chatMaybe, (user, chat) -> {

      if (user.isEmpty()) {

        return telegramRestApi.sendMessage(chatId, "Usuario no encontrado")
            .ignoreElement();
      }

      if (chat.isPresent()) {
        return telegramRestApi.sendMessage(chatId, "Cuenta ya enlazada")
            .ignoreElement();
      }

      final var telegramChat = TelegramChat.builder()
          .id(new TelegramChatId(chatId, userId))
          .chatId(chatId)
          .firstName(from.firstName())
          .lastName(from.lastName())
          .username(from.username())
          .user(user.get())
          .update(new JsonObject()
              .put("from", new JsonObject(Json.encode(from)))
              .put("chat", new JsonObject(Json.encode(chatToSave)))
              .encode())
          .createdAt(DateUtil.nowZonedWithUTC())
          .build();

      return chatService.save(telegramChat)
          .ignoreElement()
          .doOnComplete(linkHandler::fire)
          .andThen(telegramRestApi.sendMessage(chatId, "Chat guardado"))
          .ignoreElement();
    }).flatMapCompletable(c -> c);


  }
}
