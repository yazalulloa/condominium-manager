package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Completable;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationService {


  private final long chatId;

  private final TelegramRestApi restApi;

  @Autowired
  public NotificationService(@Value("${app.notification_chat_id}") long chatId, TelegramRestApi restApi) {
    this.chatId = chatId;
    this.restApi = restApi;
  }

  public boolean sendBlocking(String msg) {
    return send(msg)
        .blockingAwait(10, TimeUnit.SECONDS);
  }

  public Completable send(String msg) {
    return restApi.sendMessage(chatId, msg)
        .ignoreElement();
  }

}
