package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.pdf.CreatePdfAptReceipt;
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
import java.util.List;
import java.util.UUID;

@Service
public class CreatePdfReceiptService {

    private final ReceiptService receiptService;
    private final BuildingService buildingService;
    private final ApartmentService apartmentService;

    @Autowired
    public CreatePdfReceiptService(ReceiptService receiptService, BuildingService buildingService, ApartmentService apartmentService) {
        this.receiptService = receiptService;
        this.buildingService = buildingService;
        this.apartmentService = apartmentService;
    }

    public Single<List<CreatePdfAptReceipt>> createFiles(Long receiptId) {

        return receiptService.get(receiptId)
                .flatMap(this::createFiles);

    }


    public Single<List<CreatePdfAptReceipt>> createFiles(Receipt receipt) {
        return Single.defer(() -> {
            final var path = Paths.get("tmp/" + receipt.buildingId() + "/" + UUID.randomUUID() + "/");
            Files.createDirectories(path);


            final var buildingSingle = buildingService.get(receipt.buildingId());

            final var apartmentsByBuilding = apartmentService.rxApartmentsByBuilding(receipt.buildingId());

            return Single.zip(buildingSingle, apartmentsByBuilding, (building, apartments) -> {

                        return apartments.stream()
                                .<CreatePdfAptReceipt>map(apartment -> {
                                    return CreatePdfAptReceipt.builder()
                                            .title("AVISO DE COBRO")
                                            .path(path.resolve(apartment.apartmentId().number() + ".pdf"))
                                            .receipt(receipt)
                                            .apartment(apartment)
                                            .building(building)
                                            .build();

                                })
                                .toList();

                    })
                    .flatMapObservable(Observable::fromIterable)
                    .doOnNext(CreatePdfAptReceipt::createPdf)
                    .toList();
        });
    }

    public String fileName(Receipt receipt) {
        return receipt.buildingId() + "_" + receipt.date() + ".zip";
    }

    public Single<String> zip(Receipt receipt) {
        return createFiles(receipt)
                .map(list -> {

                    final var path = "tmp/" + receipt.buildingId() + "/" + fileName(receipt);
                    final var files = list.stream().map(CreatePdfAptReceipt::path)
                            .map(Path::toFile)
                            .toList();

                    ZipUtility.zip(files, path);
                    return path;
                });

    }
}
