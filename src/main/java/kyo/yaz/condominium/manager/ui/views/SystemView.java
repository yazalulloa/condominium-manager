package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.micrometer.core.instrument.MeterRegistry;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.security.PermitAll;
import java.util.concurrent.TimeUnit;
import kyo.yaz.condominium.manager.core.util.EnvUtil;
import kyo.yaz.condominium.manager.core.util.SystemUtil;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle(SystemView.PAGE_TITLE)
@PermitAll
@Route(value = "system_info", layout = MainLayout.class)
public class SystemView extends BaseVerticalLayout {

  public static final String PAGE_TITLE = "Sistema";

  private final Span ipSpan = new Span();
  private final Span processorsSpan = new Span();
  private final Span maxMemorySpan = new Span();
  private final Span totalMemorySpan = new Span();
  private final Span freeMemorySpan = new Span();
  private final Span usedMemorySpan = new Span();

  private final MeterRegistry meterRegistry;

  @Autowired
  public SystemView(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    init();
  }

  private void init() {
    add(ipSpan, processorsSpan, maxMemorySpan, totalMemorySpan, freeMemorySpan, usedMemorySpan);
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

      final var ip = EnvUtil.currentIp();
      final var processorsStr = SystemUtil.processorsStr();
      final var maxMemoryStr = SystemUtil.maxMemoryStr();
      final var totalMemoryStr = SystemUtil.totalMemoryStr();
      final var freeMemoryStr = SystemUtil.freeMemoryStr();
      final var usedMemoryStr = SystemUtil.usedMemoryStr();

      //meterRegistry.forEachMeter(meter -> logger().info("METER {}", meter.getId().toString()));

      uiAsyncAction(() -> {
        ipSpan.setText("IP: " + ip);
        processorsSpan.setText(processorsStr);
        maxMemorySpan.setText(maxMemoryStr);
        totalMemorySpan.setText(totalMemoryStr);
        freeMemorySpan.setText(freeMemoryStr);
        usedMemorySpan.setText(usedMemoryStr);
      });



    });
  }
}
