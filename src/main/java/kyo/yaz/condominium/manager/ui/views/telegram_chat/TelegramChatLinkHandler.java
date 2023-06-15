package kyo.yaz.condominium.manager.ui.views.telegram_chat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class TelegramChatLinkHandler {

  private final Set<Listener> listeners = new LinkedHashSet<>();

  public boolean addListener(Listener listener) {
    return listeners.add(listener);
  }

  public boolean removeListener(Listener listener) {
    return listeners.remove(listener);
  }

  public void fire() {
    listeners.forEach(Listener::chatLinked);
  }

  public interface Listener {

    void chatLinked();
  }

}
