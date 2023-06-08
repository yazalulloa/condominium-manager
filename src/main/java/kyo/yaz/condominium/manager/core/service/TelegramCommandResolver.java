package kyo.yaz.condominium.manager.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import kyo.yaz.condominium.manager.core.domain.telegram.Chat;
import kyo.yaz.condominium.manager.core.domain.telegram.TelegramUpdate;
import kyo.yaz.condominium.manager.core.domain.telegram.TelegramUser;
import kyo.yaz.condominium.manager.core.service.entity.TelegramChatService;
import kyo.yaz.condominium.manager.core.service.entity.UserService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.entity.TelegramChat;
import kyo.yaz.condominium.manager.persistence.entity.TelegramChat.TelegramChatId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelegramCommandResolver {

  private final TelegramChatService chatService;
  private final UserService userService;
  private final TelegramRestApi telegramRestApi;

  public Completable resolve(JsonNode json) {
    return Completable.defer(() -> {
      final var update = DatabindCodec.mapper().treeToValue(json, TelegramUpdate.class);

      final var message = update.message();

      if (message != null) {
        final var text = message.text();
        final var from = message.from();
        final var chat = message.chat();

        if (text != null && text.startsWith("/start") && text.length() > 8 && from != null && !from.isBot()
            && chat != null) {
          final var userId = text.substring(7).trim();

          return addAccount(userId, from, chat);
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
          .andThen(telegramRestApi.sendMessage(chatId, "Chat guardado"))
          .ignoreElement();
    }).flatMapCompletable(c -> c);


  }
}
