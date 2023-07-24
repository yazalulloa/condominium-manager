package kyo.yaz.condominium.manager.ui.views.receipt.service;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.core.pdf.CreatePdfAptReceipt;
import kyo.yaz.condominium.manager.core.pdf.CreatePdfBuildingReceipt;
import kyo.yaz.condominium.manager.core.pdf.CreatePdfReceipt;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.core.service.GetReceiptName;
import kyo.yaz.condominium.manager.core.service.entity.ApartmentService;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.core.service.entity.CalculateReceiptInfo;
import kyo.yaz.condominium.manager.core.util.ZipUtility;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.receipt.domain.ReceiptPdfProgressState;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.firitin.components.DynamicFileDownloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class GetPdfReceipts {
    private final TranslationProvider translationProvider;
    private final CalculateReceiptInfo calculateReceiptInfo;
    private final BuildingService buildingService;
    private final ApartmentService apartmentService;
    private final GetReceiptName getReceiptName;
    private final BehaviorSubject<ReceiptPdfProgressState> asyncSubject = BehaviorSubject.create();

    public Observable<ReceiptPdfProgressState> asyncSubject() {
        return asyncSubject;
    }

    public Single<Vector<PdfReceiptItem>> pdfItems(Receipt receipt) {
        final var apartmentsByBuilding = apartmentService.rxApartmentsByBuilding(receipt.buildingId());

        final var buildingSingle = buildingService.get(receipt.buildingId());


        asyncSubject.onNext(ReceiptPdfProgressState.ofIndeterminate("Buscando data..."));
        return Single.zip(buildingSingle, apartmentsByBuilding, (building, apartments) -> {

            return pdfItems(receipt, building, apartments);
        });
    }

    private Vector<PdfReceiptItem> pdfItems(Receipt receipt, Building building, List<Apartment> apartments) throws IOException {
        String tempPath = "tmp/" + UUID.randomUUID() + "/";
        asyncSubject.onNext(ReceiptPdfProgressState.ofIndeterminate("Calculando..."));
        final var receiptCalculated = calculateReceiptInfo.calculate(receipt, building, apartments);
        final var pdfReceipts = pdfReceipts(tempPath, receiptCalculated, building, apartments);


        var counter = 0;

        updateState("Creando archivos ", 0, pdfReceipts.size());
        final var vector = new Vector<PdfReceiptItem>(pdfReceipts.size());

        for (CreatePdfReceipt pdfReceipt : pdfReceipts) {
            pdfReceipt.createPdf();
            final var buildingName = pdfReceipt.building().name();

            final var apt = Optional.ofNullable(pdfReceipt.apartment())
                    .map(Apartment::apartmentId)
                    .map(Apartment.ApartmentId::number)
                    .orElse("");

            updateState("Creando archivos %s %s ".formatted(buildingName, apt), ++counter, pdfReceipts.size());

            final var fileName = Optional.ofNullable(pdfReceipt.apartment())
                    .map(Apartment::apartmentId)
                    .map(Apartment.ApartmentId::number)
                    .orElse(pdfReceipt.building().name());

            final var item = new PdfReceiptItem(pdfReceipt.path(), "%s.pdf".formatted(fileName),
                    pdfReceipt.id(),
                    pdfReceipt.building().name(),
                    Optional.ofNullable(pdfReceipt.apartment()).map(Apartment::emails).orElse(null));

            vector.add(item);
        }

        return vector;
    }

    private void updateState(String title, int value, int max) {
        final var endText = " %s/%s".formatted(value, max);

        final var state = ReceiptPdfProgressState.builder()
                .indeterminate(false)
                .min(0)
                .value(value)
                .max(max)
                .text(title)
                .endText(endText)
                .build();

        asyncSubject.onNext(state);
    }

    private List<CreatePdfReceipt> pdfReceipts(String tempPath, Receipt receipt, Building building, List<Apartment> apartments) throws IOException {

        final var path = Paths.get(tempPath + receipt.buildingId() + "/");
        Files.createDirectories(path);
        final var list = new LinkedList<CreatePdfReceipt>();

        final var buildingPdfReceipt = CreatePdfBuildingReceipt.builder()
                .translationProvider(translationProvider)
                .path(path.resolve(building.id() + ".pdf"))
                .receipt(receipt)
                .building(building)
                .build();

        list.add(buildingPdfReceipt);

        apartments.stream()
                .<CreatePdfReceipt>map(apartment -> {
                    return CreatePdfAptReceipt.builder()
                            .translationProvider(translationProvider)
                            .title("AVISO DE COBRO")
                            .path(path.resolve(apartment.apartmentId().number() + ".pdf"))
                            .receipt(receipt)
                            .apartment(apartment)
                            .building(building)
                            .build();

                })
                .forEach(list::add);

        return list;
    }

    public String zipPath(Receipt receipt, Collection<PdfReceiptItem> list) throws IOException {
        asyncSubject.onNext(ReceiptPdfProgressState.ofIndeterminate("Creando zip..."));


        final var dirPath = "tmp/" + receipt.buildingId() + "/";
        final var zipPath = dirPath + getReceiptName.zipFileName(receipt);
        Files.createDirectories(Paths.get(dirPath));
        final var files = list.stream().map(PdfReceiptItem::path)
                .map(Path::toFile)
                .toList();

        ZipUtility.zip(files, zipPath);

        asyncSubject.onNext(ReceiptPdfProgressState.ofIndeterminate("Descargando..."));
        return zipPath;
    }

    public Single<String> zipReceipt(Receipt receipt) {
        final var apartmentsByBuilding = apartmentService.rxApartmentsByBuilding(receipt.buildingId());

        final var buildingSingle = buildingService.get(receipt.buildingId());


        asyncSubject.onNext(ReceiptPdfProgressState.ofIndeterminate("Buscando data..."));
        return Single.zip(buildingSingle, apartmentsByBuilding, (building, apartments) -> {

            final var pdfReceiptItems = pdfItems(receipt, building, apartments);
            return zipPath(receipt, pdfReceiptItems);
        });
    }

    public DynamicFileDownloader fileDownloader(Receipt receipt) {
        final var fileDownloader = new DynamicFileDownloader("", getReceiptName.zipFileName(receipt), outputStream -> {

            final var path = zipReceipt(receipt)
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
            asyncSubject.onNext(ReceiptPdfProgressState.visible(false));
        });

        fileDownloader.add(new Icon(VaadinIcon.DOWNLOAD_ALT));
        return fileDownloader;
    }
}
