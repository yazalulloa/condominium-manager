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
  private final SendLogs sendLogs;

  @Autowired
  public NotificationService(@Value("${app.notification_chat_id}") long chatId, TelegramRestApi restApi,
      SendLogs sendLogs) {
    this.chatId = chatId;
    this.restApi = restApi;
    this.sendLogs = sendLogs;
  }

  public boolean sendBlocking(String msg) {
    return blocking(send(msg));
  }

  public Completable send(String msg) {
    return restApi.sendMessage(chatId, msg)
        .ignoreElement();
  }

  private boolean blocking(Completable completable) {
    return completable
        .blockingAwait(10, TimeUnit.SECONDS);
  }

  public Completable sendLogs(String caption) {
    return sendLogs.sendLogs(chatId, caption);
  }

  public boolean sendLogsBlocking(String caption) {
    return blocking(sendLogs(caption));
  }
}
