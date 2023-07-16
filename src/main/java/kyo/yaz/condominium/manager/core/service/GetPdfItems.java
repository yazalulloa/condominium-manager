package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;

import java.util.List;
import java.util.function.Consumer;

public interface GetPdfItems {
    Single<List<PdfReceiptItem>> pdfItems(Receipt receipt);

    Single<List<PdfReceiptItem>> pdfItems(Receipt receipt, Building building, List<Apartment> apartments);

    void setPlConsumer(Consumer<Consumer<ProgressLayout>> plConsumer);

    void delete();
}
