package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/*@Service
public class GetPdfItemsImpl implements GetPdfItems {

    private final CreatePdfReceiptService pdfReceiptService;

    @Autowired
    public GetPdfItemsImpl(CreatePdfReceiptService pdfReceiptService) {
        this.pdfReceiptService = pdfReceiptService;
    }

    @Override
    public Single<List<PdfReceiptItem>> pdfItems(Receipt receipt) {
        return pdfReceiptService.createFiles(receipt, false)
                .flatMapObservable(Observable::fromIterable)
                .map(createPdfReceipt -> new PdfReceiptItem(createPdfReceipt.path(), createPdfReceipt.path().getFileName().toString(),
                        createPdfReceipt.id()))
                .toList(LinkedList::new);
    }
}*/
