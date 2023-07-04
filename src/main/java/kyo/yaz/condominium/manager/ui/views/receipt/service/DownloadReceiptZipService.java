package kyo.yaz.condominium.manager.ui.views.receipt.service;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.Vertx;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.core.service.CreatePdfReceiptService;
import kyo.yaz.condominium.manager.core.service.GetPdfItems;
import kyo.yaz.condominium.manager.core.util.ZipUtility;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.actions.DownloadReceiptZipAction;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.vaadin.firitin.components.DynamicFileDownloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class DownloadReceiptZipService {

    private final Vertx vertx;
    private final CreatePdfReceiptService createPdfReceiptService;
    private Consumer<Consumer<ProgressLayout>> plConsumer;
    private final GetPdfItems getPdfItems;

  /*  @Autowired
    public DownloadReceiptZipService(Vertx vertx, CreatePdfReceiptService createPdfReceiptService, GetPdfItems getPdfItems) {
        this.vertx = vertx;
        this.createPdfReceiptService = createPdfReceiptService;
        this.getPdfItems = getPdfItems;
    }*/


    public void setPlConsumer(Consumer<Consumer<ProgressLayout>> plConsumer) {
        this.plConsumer = plConsumer;
    }

    private void startProgress() {
        updateProgress(pl -> {
            pl.setProgressText("Creando archivos");
            pl.progressBar().setIndeterminate(true);
            pl.setVisible(true);
        });
    }

    private final DownloadReceiptZipAction<Receipt> zipAction = new DownloadReceiptZipAction<>() {
        @Override
        public void downloadBtnClicked() {
            startProgress();
        }

        @Override
        public String fileName(Receipt obj) {
            return createPdfReceiptService.fileName(obj);
        }

        @Override
        public String filePath(Receipt receipt) {

            return zipPath(receipt)
                    .subscribeOn(Schedulers.io())
                    .doAfterTerminate(getPdfItems::delete)
                    .blockingGet();
        }

        @Override
        public void downloadFinished(Runnable runnable) {
            vertx.setTimer(TimeUnit.SECONDS.toMillis(2), l -> {
                updateProgress(pl -> {

                    pl.setVisible(false);
                    runnable.run();
                });
            });
        }
    };

    public String zipPath(Receipt receipt, Collection<PdfReceiptItem> list) throws IOException {
        updateProgress(pl -> {
            pl.setProgressText("Creando zip");
            pl.progressBar().setIndeterminate(true);
        });

        final var dirPath = "tmp/" + receipt.buildingId() + "/";
        final var zipPath = dirPath + zipAction.fileName(receipt);
        Files.createDirectories(Paths.get(dirPath));
        final var files = list.stream().map(PdfReceiptItem::path)
                .map(Path::toFile)
                .toList();

        ZipUtility.zip(files, zipPath);
        updateProgress(pl -> pl.setProgressText("Descargando"));
        return zipPath;
    }

    public Single<String> zipPath(Receipt receipt) {
        getPdfItems.setPlConsumer(plConsumer);

        return getPdfItems.pdfItems(receipt)
                .map(list -> zipPath(receipt, list));
    }

    public DynamicFileDownloader fileDownloader(Receipt receipt) {
        final var fileDownloader = new DynamicFileDownloader("", zipAction.fileName(receipt), outputStream -> {
            startProgress();

            final var path = zipPath(receipt)
                    .subscribeOn(Schedulers.io())
                    .blockingGet();

            try (var inputStream = new BufferedInputStream(new FileInputStream(path));
                 var targetStream = new BufferedOutputStream(outputStream)) {
                inputStream.transferTo(targetStream);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        fileDownloader.addClassName("file-downloader");
        fileDownloader.setDisableOnClick(true);

        fileDownloader.addDownloadFinishedListener(e -> {
            fileDownloader.setEnabled(true);
            updateProgress(pl -> pl.setVisible(false));
        });

        fileDownloader.add(new Icon(VaadinIcon.DOWNLOAD_ALT));
        return fileDownloader;
    }

    private void updateProgress(Consumer<ProgressLayout> consumer) {
        if (plConsumer != null) {
            plConsumer.accept(consumer);
        }
    }
}
