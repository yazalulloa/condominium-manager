package kyo.yaz.condominium.manager.ui.views.base;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

public interface AbstractView {

    Component component();

    Logger logger();

    default void showError(Throwable throwable) {

        asyncNotification("Error" + throwable.getMessage());
        logger().error("ERROR", throwable);
    }

    default <T> Subscriber<T> subscriber(Runnable runnable) {
        return ViewUtil.subscriber(runnable, this::showError);
    }

    default Subscriber<Void> emptySubscriber() {
        return ViewUtil.emptySubscriber(this::showError);
    }

    default void asyncNotification(String message) {
        uiAsyncAction(() -> Notification.show(message));
    }

    default void ui(Consumer<UI> uiConsumer) {
        component().getUI().ifPresent(uiConsumer);
    }

    default void uiAsyncAction(Collection<Runnable> runnable) {
        uiAsyncAction(() -> runnable.forEach(Runnable::run));
    }

    default void uiAsyncAction(Runnable... runnable) {
        uiAsyncAction(Arrays.asList(runnable));
    }

    default void uiAsyncAction(Runnable runnable) {
       if (runnable != null) {
           ui(ui -> {
               ui.access(() -> {
                   runnable.run();
                   ui.push();
               });
           });
       }
    }
}
