package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;

import java.util.List;

public interface GetPdfItems {
    Single<List<PdfReceiptItem>> pdfItems(Receipt receipt);
}
