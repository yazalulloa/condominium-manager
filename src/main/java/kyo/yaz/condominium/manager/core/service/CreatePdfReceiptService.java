package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.core.pdf.CreateBuildingPdfReceipt;
import kyo.yaz.condominium.manager.core.pdf.CreatePdfAptReceipt;
import kyo.yaz.condominium.manager.core.pdf.CreatePdfReceipt;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.core.service.entity.ApartmentService;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.core.service.entity.ReceiptService;
import kyo.yaz.condominium.manager.core.util.ZipUtility;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
public class CreatePdfReceiptService {

    private final ReceiptService receiptService;
    private final BuildingService buildingService;
    private final ApartmentService apartmentService;
    private final DeleteDirAfterDelay deleteDirAfterDelay;
    private final TranslationProvider translationProvider;

    @Autowired
    public CreatePdfReceiptService(ReceiptService receiptService, BuildingService buildingService, ApartmentService apartmentService, DeleteDirAfterDelay deleteDirAfterDelay, TranslationProvider translationProvider) {
        this.receiptService = receiptService;
        this.buildingService = buildingService;
        this.apartmentService = apartmentService;
        this.deleteDirAfterDelay = deleteDirAfterDelay;
        this.translationProvider = translationProvider;
    }

    public Single<List<CreatePdfReceipt>> createFiles(Long receiptId) {

        return receiptService.get(receiptId)
                .flatMap(this::createFiles);

    }

    public Single<List<PdfReceiptItem>> pdfItems(Receipt receipt) {
        return createFiles(receipt, false)
                .flatMapObservable(Observable::fromIterable)
                .map(createPdfReceipt -> new PdfReceiptItem(createPdfReceipt.path(), createPdfReceipt.path().getFileName().toString(),
                        createPdfReceipt.id()))
                .toList(LinkedList::new)
                /*.reduceWith(LinkedHashMap::new, (map, item) -> {
                    map.put(item.id(), item);
                    return map;
                })*/
                ;
    }

    public Single<List<CreatePdfReceipt>> createFiles(Receipt receipt, boolean shouldDeleteAfter) {
        return Single.defer(() -> {
            final var tempPath = "tmp/" + UUID.randomUUID() + "/";
            final var path = Paths.get(tempPath + receipt.buildingId() + "/");
            Files.createDirectories(path);

            final var buildingSingle = buildingService.get(receipt.buildingId());

            final var apartmentsByBuilding = apartmentService.rxApartmentsByBuilding(receipt.buildingId());

            return Single.zip(buildingSingle, apartmentsByBuilding, (building, apartments) -> {

                        final var list = new LinkedList<CreatePdfReceipt>();

                        final var buildingPdfReceipt = CreateBuildingPdfReceipt.builder()
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
                    })
                    .flatMapObservable(Observable::fromIterable)
                    .doOnNext(CreatePdfReceipt::createPdf)
                    .toList(LinkedList::new)
                    .doAfterTerminate(() -> {
                        if (shouldDeleteAfter) {
                            deleteDirAfterDelay.deleteDir(tempPath);
                        }
                    });
        });
    }

    public Single<List<CreatePdfReceipt>> createFiles(Receipt receipt) {
        return createFiles(receipt, true);
    }

    public String fileName(Receipt receipt) {
        return receipt.buildingId() + "_" + receipt.date() + ".zip";
    }

    public Single<String> zip(Receipt receipt) {
        return createFiles(receipt)
                .map(list -> {

                    final var dirPath = "tmp/" + receipt.buildingId() + "/";
                    final var path = dirPath + fileName(receipt);
                    Files.createDirectories(Paths.get(dirPath));
                    final var files = list.stream().map(CreatePdfReceipt::path)
                            .map(Path::toFile)
                            .toList();

                    ZipUtility.zip(files, path);
                    return path;
                })
                .doAfterSuccess(deleteDirAfterDelay::deleteDir);

    }
}
