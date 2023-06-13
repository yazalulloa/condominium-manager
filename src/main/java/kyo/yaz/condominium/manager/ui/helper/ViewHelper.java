package kyo.yaz.condominium.manager.ui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import lombok.AllArgsConstructor;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.function.Consumer;

@AllArgsConstructor
public class ViewHelper {
    private final Component component;
    private final Logger logger;

    public void showError(Throwable throwable) {
        showError(throwable, "");
    }

    public void showError(Throwable throwable, String tag) {
        showError("Error " + tag + " " + throwable.getMessage());
        logger.error("ERROR " + tag + " ", throwable);
    }

    public void showError(String errorMessage) {
        final var notification = new Notification(errorMessage, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        asyncNotification(notification);
    }

    public void showNotification(String text, int duration, Notification.Position position) {
        asyncNotification(new Notification(text, duration, position));
    }

    public <T> Subscriber<T> subscriber(Runnable runnable) {
        return ViewUtil.subscriber(runnable, this::showError);
    }

    public Subscriber<Void> emptySubscriber() {
        return emptySubscriber("");
    }

    public Subscriber<Void> emptySubscriber(String tag) {
        return ViewUtil.emptySubscriber(t -> showError(t, tag));
    }

    public void asyncNotification(Notification notification) {

        uiAsyncAction(notification::open);
    }

    public void asyncNotification(String message) {
        asyncNotification(new Notification(message, 3000));
    }

    public void ui(Consumer<UI> uiConsumer) {
        component.getUI().ifPresent(uiConsumer);
    }

    public void uiAsyncAction(Iterable<Runnable> runnable) {
        uiAsyncAction(() -> runnable.forEach(Runnable::run));
    }

    public void uiAsyncAction(Runnable... runnable) {
        uiAsyncAction(Arrays.asList(runnable));
    }

    public void uiAsyncAction(Runnable runnable) {
        if (runnable != null) {
            ui(ui -> {

                if (ui.isAttached()) {
                    ui.access(() -> {
                        runnable.run();
                        ui.push();
                    });
                } else {
                    logger.info("UI is not attached");
                }

            });
        }
    }


}
