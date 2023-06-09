package kyo.yaz.condominium.manager.core.component;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import kyo.yaz.condominium.manager.core.service.NotificationService;
import kyo.yaz.condominium.manager.core.util.EnvUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServletContextListenerImpl implements ServletContextListener {

  private final NotificationService notificationService;

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    sendMsg("Shutting down");
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {
    log.info("contextInitialized");
  }

  private void sendMsg(String msg) {
    if (EnvUtil.sendNotifications()) {
      notificationService.sendBlocking(msg);
    }
  }

  public void addHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      sendMsg("Shutting down");
    }));
  }

  @EventListener(ApplicationReadyEvent.class)
  public void doSomethingAfterStartup() {
    sendMsg("App Started Up");
  }
}
