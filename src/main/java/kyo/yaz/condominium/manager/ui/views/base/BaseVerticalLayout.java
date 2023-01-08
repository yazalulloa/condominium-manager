package kyo.yaz.condominium.manager.ui.views.base;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import kyo.yaz.condominium.manager.core.util.RxUtil;
import kyo.yaz.condominium.manager.ui.helper.ViewHelper;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseVerticalLayout extends VerticalLayout {

    protected final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected final ViewHelper viewHelper = new ViewHelper(this, logger);

    protected final Component component() {
        return this;
    }


    protected final Logger logger() {
        return logger;
    }


    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        compositeDisposable.dispose();
    }

    protected CompletableObserver completableObserver() {
        return completableObserver(() -> {
        });
    }

    protected CompletableObserver completableObserver(@NonNull Action onComplete) {
        return completableObserver(onComplete, this::showError);

    }

    protected CompletableObserver completableObserver(@NonNull Action onComplete, @NonNull Consumer<? super Throwable> onError) {
        return RxUtil.completableObserver(compositeDisposable::add, onComplete, onError);
    }

    protected <T> SingleObserver<T> singleObserver(@NonNull Consumer<? super T> onSuccess, @NonNull Consumer<? super Throwable> onError) {
        return RxUtil.singleObserver(compositeDisposable::add, onSuccess, onError);
    }

    protected void showError(Throwable throwable) {
        viewHelper.showError(throwable);
    }

    protected void showError(Throwable throwable, String tag) {
        viewHelper.showError(throwable, tag);
    }

    protected <T> Subscriber<T> subscriber(Runnable runnable) {
        return viewHelper.subscriber(runnable);
    }

    protected Subscriber<Void> emptySubscriber() {
        return viewHelper.emptySubscriber("");
    }

    protected Subscriber<Void> emptySubscriber(String tag) {
        return viewHelper.emptySubscriber(tag);
    }

    protected void asyncNotification(String message) {
        viewHelper.asyncNotification(message);
    }

    protected void ui(java.util.function.Consumer<UI> uiConsumer) {
        viewHelper.ui(uiConsumer);
    }

    protected void uiAsyncAction(Iterable<Runnable> runnable) {
        viewHelper.uiAsyncAction(runnable);
    }

    protected void uiAsyncAction(Runnable... runnable) {
        viewHelper.uiAsyncAction(runnable);
    }

    protected void uiAsyncAction(Runnable runnable) {
        viewHelper.uiAsyncAction(runnable);
    }
}
