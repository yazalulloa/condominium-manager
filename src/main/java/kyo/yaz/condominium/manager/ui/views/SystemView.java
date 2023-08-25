package kyo.yaz.condominium.manager.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.micrometer.core.instrument.MeterRegistry;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.security.PermitAll;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import kyo.yaz.condominium.manager.core.domain.FileResponse;
import kyo.yaz.condominium.manager.core.service.HttpService;
import kyo.yaz.condominium.manager.core.service.LogService;
import kyo.yaz.condominium.manager.core.service.entity.EntityDownloader;
import kyo.yaz.condominium.manager.core.util.SystemUtil;
import kyo.yaz.condominium.manager.core.util.ZipUtility;
import kyo.yaz.condominium.manager.ui.MainLayout;
import kyo.yaz.condominium.manager.ui.views.base.BaseVerticalLayout;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.vaadin.firitin.components.DynamicFileDownloader;

@PageTitle(SystemView.PAGE_TITLE)
@PermitAll
@Route(value = "system_info", layout = MainLayout.class)
public class SystemView extends BaseVerticalLayout {

  public static final String PAGE_TITLE = "Sistema";

  private final ComboBox<Integer> refreshRateSystemInfoComboBox = ViewUtil.refreshRateSystemInfoComboBox();

  private final List<Pair<Paragraph, Supplier<String>>> paragraphs = new ArrayList<>();
  private final ProgressLayout progressLayout = new ProgressLayout();

  private final HttpService httpService;
  private final LogService logService;
  private final MeterRegistry meterRegistry;
  private final Map<String, EntityDownloader> backupDownloaderMap;

  private Disposable disposable;

  @Autowired
  public SystemView(HttpService httpService, MeterRegistry meterRegistry, LogService logService,
      Map<String, EntityDownloader> backupDownloaderMap) {
    this.httpService = httpService;
    this.meterRegistry = meterRegistry;
    this.logService = logService;
    this.backupDownloaderMap = backupDownloaderMap;
    init();
  }

  private void setDisposable(Disposable disposable) {
    if (this.disposable != null) {
      this.disposable.dispose();
    }

    this.disposable = disposable;
  }

  private void init() {

    refreshRateSystemInfoComboBox.addValueChangeListener(e -> {
      final var value = e.getValue();
      final var oldValue = e.getOldValue();

      //logger().info("value changed");
      if (!Objects.equals(value, oldValue)) {
        //logger().info("value changed after equals");
        systemInfo();
      }


    });

    paragraphs.add(pair(SystemUtil::ipStr));
    paragraphs.add(pair(() -> "HTTP REQUEST COUNT %s".formatted(httpService.requestCount().blockingGet())));
    paragraphs.add(pair(SystemUtil::processorsStr));
    paragraphs.add(pair(SystemUtil::maxMemoryStr));
    paragraphs.add(pair(SystemUtil::totalMemoryStr));
    paragraphs.add(pair(SystemUtil::freeMemoryStr));
    paragraphs.add(pair(SystemUtil::usedMemoryStr));
    paragraphs.add(pair(SystemUtil::freeSpaceStr));
    paragraphs.add(pair(SystemUtil::usableSpaceStr));
    paragraphs.add(pair(SystemUtil::totalSpaceStr));
    paragraphs.add(pair(SystemUtil::usedSpaceStr));

    progressLayout.setVisible(false);
    add(getToolbar(), progressLayout);
    paragraphs.stream().map(Pair::getFirst)
        .forEach(this::add);
  }

  private HorizontalLayout getToolbar() {

    final var logsDownloader = new DynamicFileDownloader("Descargar Logs", "logs.zip", outputStream -> {

      try {
        uiAsyncAction(() -> {
          progressLayout.setProgressText("Comprimiendo logs");
          progressLayout.progressBar().setIndeterminate(true);
          progressLayout.setVisible(true);
        });

        final var path = logService.zipLogs();
        try (var inputStream = new BufferedInputStream(new FileInputStream(path));
            var targetStream = new BufferedOutputStream(outputStream)) {
          inputStream.transferTo(targetStream);

        }
      } catch (Exception e) {
        uiAsyncAction(() -> showError(e));
      } finally {
        uiAsyncAction(() -> progressLayout.setVisible(false));
      }

    }).asButton();

    final var backupFileName = "cm-backup.tar.gz";
    final var backupDownloader = new DynamicFileDownloader("Descargar backups", backupFileName, outputStream -> {

      try {
        uiAsyncAction(() -> {
          progressLayout.setProgressText("Descargando");
          progressLayout.progressBar().setIndeterminate(true);
          progressLayout.setVisible(true);
        });

        final var path = Observable.fromIterable(backupDownloaderMap.entrySet())
            .map(Map.Entry::getValue)
            .map(EntityDownloader::download)
            .toList()
            .toFlowable()
            .flatMap(Single::merge)
            .toList()
            .map(list -> {

              uiAsyncAction(() -> {
                progressLayout.setProgressText("Comprimiendo logs");
                progressLayout.progressBar().setIndeterminate(true);
                progressLayout.setVisible(true);
              });

              final var set = list.stream().map(FileResponse::path).collect(Collectors.toSet());
              final var tempFile = "tmp/" + System.currentTimeMillis() + "/";
              Files.createDirectories(Paths.get(tempFile));
              final var filePath = tempFile + backupFileName;
              ZipUtility.createTarGzipFiles(filePath, set);

              return filePath;
            })
            .subscribeOn(Schedulers.io())
            .blockingGet();

        try (var inputStream = new BufferedInputStream(new FileInputStream(path));
            var targetStream = new BufferedOutputStream(outputStream)) {
          inputStream.transferTo(targetStream);

        }
      } catch (Exception e) {
        uiAsyncAction(() -> showError(e));
      } finally {
        uiAsyncAction(() -> {
          progressLayout.setVisible(false);


        });
      }

    }).asButton();

    return new HorizontalLayout(refreshRateSystemInfoComboBox, logsDownloader, backupDownloader);
  }

  private Pair<Paragraph, Supplier<String>> pair(Supplier<String> supplier) {
    return Pair.of(new Paragraph(), supplier);
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    systemInfo();

  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    setDisposable(null);
  }

  private void systemInfo() {
    final var value = refreshRateSystemInfoComboBox.getValue();
    loadSystemInfo()
        //.doOnComplete(() -> logger().info("Refreshing {}", value))
        .delay(value, TimeUnit.SECONDS)
        .doOnError(throwable -> logger().error("Failed to load system info", throwable))
        .onErrorComplete()
        .repeat()
        .subscribeOn(Schedulers.computation())
        .subscribe(completableObserver(this::setDisposable));
  }

  private Completable loadSystemInfo() {
    return Completable.fromAction(() -> {
      //  logger().info("Loading system info");
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
