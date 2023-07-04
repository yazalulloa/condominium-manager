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
        log.info("contextDestroyed {}", Thread.currentThread().getName());
        checkIfSend(notificationService::sendShuttingDownApp);
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        log.info("contextInitialized {}", Thread.currentThread().getName());
    }

    private void checkIfSend(Runnable runnable) {
        if (EnvUtil.sendNotifications()) {
            runnable.run();
        }
    }

    public void addHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("ShutdownHook {}", Thread.currentThread().getName());
        }));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        checkIfSend(notificationService::sendAppStartup);

    }
}
