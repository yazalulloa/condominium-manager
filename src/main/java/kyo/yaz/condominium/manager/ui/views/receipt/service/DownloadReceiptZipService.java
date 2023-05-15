package kyo.yaz.condominium.manager.ui.views.receipt.service;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.Vertx;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.core.service.CreatePdfReceiptService;
import kyo.yaz.condominium.manager.core.service.GetPdfItems;
import kyo.yaz.condominium.manager.core.util.ZipUtility;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.actions.DownloadReceiptZipAction;
import kyo.yaz.condominium.manager.ui.views.component.DownloadReceiptZipAnchor;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Slf4j
public class DownloadReceiptZipService {

    private final Vertx vertx;
    private final CreatePdfReceiptService createPdfReceiptService;
    private Consumer<Consumer<ProgressLayout>> plConsumer;
    private final GetPdfItems getPdfItems;

    @Autowired
    public DownloadReceiptZipService(Vertx vertx, CreatePdfReceiptService createPdfReceiptService, GetPdfItems getPdfItems) {
        this.vertx = vertx;
        this.createPdfReceiptService = createPdfReceiptService;
        this.getPdfItems = getPdfItems;
    }


    public void setPlConsumer(Consumer<Consumer<ProgressLayout>> plConsumer) {
        this.plConsumer = plConsumer;
    }

    public ComponentRenderer<Anchor, Receipt> downloadAnchor() {
        return new ComponentRenderer<>(Anchor::new, new DownloadReceiptZipAnchor(new DownloadReceiptZipAction<>() {
            @Override
            public void downloadBtnClicked() {
                updateProgress(pl -> {
                    pl.setProgressText("Creando archivos");
                    pl.progressBar().setIndeterminate(true);
                    pl.setVisible(true);
                });
            }

            @Override
            public String fileName(Receipt obj) {
                return createPdfReceiptService.fileName(obj);
            }

            @Override
            public String filePath(Receipt receipt) {
                getPdfItems.setPlConsumer(plConsumer);

                return getPdfItems.pdfItems(receipt)
                        .map(list -> {
                            updateProgress(pl -> {
                                pl.setProgressText("Creando zip");
                                pl.progressBar().setIndeterminate(true);
                            });

                            final var dirPath = "tmp/" + receipt.buildingId() + "/";
                            final var zipPath = dirPath + fileName(receipt);
                            Files.createDirectories(Paths.get(dirPath));
                            final var files = list.stream().map(PdfReceiptItem::path)
                                    .map(Path::toFile)
                                    .toList();

                            ZipUtility.zip(files, zipPath);
                            updateProgress(pl -> pl.setProgressText("Descargando"));
                            return zipPath;
                        })
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
        }));
    }

    private void updateProgress(Consumer<ProgressLayout> consumer) {
        if (plConsumer != null) {
            plConsumer.accept(consumer);
        }
    }
}
