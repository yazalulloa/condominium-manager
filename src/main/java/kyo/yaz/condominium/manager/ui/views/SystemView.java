package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.micrometer.core.instrument.MeterRegistry;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.security.PermitAll;
import kyo.yaz.condominium.manager.core.util.SystemUtil;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@PageTitle(SystemView.PAGE_TITLE)
@PermitAll
@Route(value = "system_info", layout = MainLayout.class)
public class SystemView extends BaseVerticalLayout {

    public static final String PAGE_TITLE = "Sistema";

    private final MeterRegistry meterRegistry;

    private final List<Pair<Paragraph, Supplier<String>>> paragraphs = new ArrayList<>();

    @Autowired
    public SystemView(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        init();
    }

    private void init() {
        paragraphs.add(pair(SystemUtil::ipStr));
        paragraphs.add(pair(SystemUtil::processorsStr));
        paragraphs.add(pair(SystemUtil::maxMemoryStr));
        paragraphs.add(pair(SystemUtil::totalMemoryStr));
        paragraphs.add(pair(SystemUtil::freeMemoryStr));
        paragraphs.add(pair(SystemUtil::usedMemoryStr));
        paragraphs.add(pair(SystemUtil::freeSpaceStr));
        paragraphs.add(pair(SystemUtil::usableSpaceStr));
        paragraphs.add(pair(SystemUtil::totalSpaceStr));
        paragraphs.add(pair(SystemUtil::usedSpaceStr));

        paragraphs.stream().map(Pair::getFirst)
                .forEach(this::add);
    }

    private Pair<Paragraph, Supplier<String>> pair(Supplier<String> supplier) {
        return Pair.of(new Paragraph(), supplier);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        loadSystemInfo()
                .delay(5, TimeUnit.SECONDS)
                .doOnError(throwable -> logger().error("Failed to load system info", throwable))
                .onErrorComplete()
                .repeat()
                .subscribeOn(Schedulers.computation())
                .subscribe(completableObserver());

    }

    private Completable loadSystemInfo() {
        return Completable.fromAction(() -> {
            final var runnable = paragraphs.stream()
                    .map(pair -> {
                        final var component = pair.getFirst();
                        final var value = pair.getSecond().get();
                        return (Runnable) () -> component.setText(value);
                    })
                    .toList();

            uiAsyncAction(runnable);


        });
    }
}
