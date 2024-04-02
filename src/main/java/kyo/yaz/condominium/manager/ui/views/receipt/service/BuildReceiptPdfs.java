package kyo.yaz.condominium.manager.ui.views.receipt.service;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.core.service.DeleteDirAfterDelay;
import kyo.yaz.condominium.manager.core.service.GetPdfItems;
import kyo.yaz.condominium.manager.core.service.entity.ApartmentService;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.core.service.entity.CalculateReceiptInfo;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class BuildReceiptPdfs implements GetPdfItems {

  private final GetPdfReceipts createPdfReceiptService;
  private final DeleteDirAfterDelay deleteDirAfterDelay;
  private final CalculateReceiptInfo calculateReceiptInfo;
  private final BuildingService buildingService;
  private final ApartmentService apartmentService;
  private String tempPath;
  private Consumer<Consumer<ProgressLayout>> plConsumer;


  public void setPlConsumer(Consumer<Consumer<ProgressLayout>> plConsumer) {
    this.plConsumer = plConsumer;
  }


  public Single<List<PdfReceiptItem>> pdfItems(Receipt receipt) {
    delete();

    tempPath = "tmp/" + UUID.randomUUID() + "/";
    final AtomicInteger i = new AtomicInteger(1);

    final var apartmentsByBuilding = apartmentService.rxApartmentsByBuilding(receipt.buildingId());

    final var buildingSingle = buildingService.get(receipt.buildingId());

    return Single.zip(buildingSingle, apartmentsByBuilding,
            (building, apartments) -> pdfItems(receipt, building, apartments))
        .flatMap(s -> s);
  }

  @Override
  public Single<List<PdfReceiptItem>> pdfItems(Receipt receipt, Building building, List<Apartment> apartments) {
    delete();

    tempPath = "tmp/" + UUID.randomUUID() + "/";
    final AtomicInteger i = new AtomicInteger(1);

    return Single.fromCallable(() -> {
          final var receiptCalculated = calculateReceiptInfo.calculate(receipt, building, apartments);

          return createPdfReceiptService.pdfReceipts(tempPath, receiptCalculated, building, apartments);
        })
        .observeOn(Schedulers.io())
        .flatMap(list -> {

          updateProgress(pl -> {

            pl.progressBar().setIndeterminate(false);
            pl.progressBar().setMin(0);
            pl.progressBar().setMax(list.size());
            updateFileCreation(pl, 0, list.size());
          });

          return Observable.fromIterable(list)
              .doOnNext(pdfReceipt -> {
                pdfReceipt.createPdf();

                final var buildingName = pdfReceipt.building().name();

                final var apt = Optional.ofNullable(pdfReceipt.apartment())
                    .map(Apartment::apartmentId)
                    .map(Apartment.ApartmentId::number)
                    .orElse("");

                final var integer = i.getAndIncrement();
                updateProgress(
                    pl -> updateFileCreation("Creando archivos %s %s ".formatted(buildingName, apt), pl, integer,
                        list.size()));
              })
              .map(pdf -> {

                final var fileName = Optional.ofNullable(pdf.apartment())
                    .map(Apartment::apartmentId)
                    .map(Apartment.ApartmentId::number)
                    .orElse(pdf.building().name());

                return new PdfReceiptItem(pdf.path(), "%s.pdf".formatted(fileName),
                    pdf.id(),
                    pdf.building().name(),
                    Optional.ofNullable(pdf.apartment()).map(Apartment::emails).orElse(null));
              })
              .toList(LinkedList::new);

        });
  }

  public void delete() {
    if (tempPath != null) {
      deleteDirAfterDelay.deleteDir(tempPath);
    }
  }

  private void updateFileCreation(ProgressLayout pl, int value, int max) {
    updateFileCreation("Creando archivos ", pl, value, max);
  }

  private void updateFileCreation(String title, ProgressLayout pl, int value, int max) {
    pl.progressBar().setValue(value);
    pl.setProgressText(title, " %s/%s".formatted(value, max));
  }

  private void updateProgress(Consumer<ProgressLayout> consumer) {
    if (plConsumer != null) {
      plConsumer.accept(consumer);
    }
  }

}
