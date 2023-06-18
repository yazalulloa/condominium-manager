package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.core.service.entity.TelegramChatService;
import kyo.yaz.condominium.manager.core.service.telegram.TelegramRestApi;
import kyo.yaz.condominium.manager.persistence.domain.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationService {


  private final TelegramRestApi restApi;
  private final SendLogs sendLogs;
  private final TelegramChatService chatService;
  private final TranslationProvider translationProvider;

  public boolean sendAppStartup() {
    final var event = NotificationEvent.APP_STARTUP;
    final var msg = translationProvider.translate(event.name());
    return blocking(send(msg, event));
  }

  private Completable sendNotification(Set<NotificationEvent> set, Function<Long, Completable> function) {
    return chatService.chatsByEvents(set)
        .filter(s -> !s.isEmpty())
        .flatMapObservable(Observable::fromIterable)
        .map(function::apply)
        .toList()
        .toFlowable()
        .flatMapCompletable(Completable::merge);
  }

  public Completable sendNewRate(String msg) {
    return send(msg, NotificationEvent.NEW_RATE);
  }

  public Completable send(String msg, NotificationEvent event) {
    return sendNotification(Set.of(event), chat -> restApi.sendMessage(chat, msg).ignoreElement());
  }

  private boolean blocking(Completable completable) {
    return completable
        .blockingAwait(10, TimeUnit.SECONDS);
  }

  public Completable sendLogs(long chatId, String caption) {
    return sendLogs.sendLogs(chatId, caption);
  }

  public boolean sendShuttingDownApp() {
    final var event = NotificationEvent.APP_SHUTTING_DOWN;
    final var caption = translationProvider.translate(event.name());
    return blocking(sendNotification(Set.of(event), chat -> sendLogs(chat, caption)));
  }
}
