package kyo.yaz.condominium.manager.core.service.hardcode;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Single;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import kyo.yaz.condominium.manager.core.domain.PdfReceiptItem;
import kyo.yaz.condominium.manager.core.service.GetPdfItems;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;
import kyo.yaz.condominium.manager.ui.views.util.HardCode;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetPdfItemsHardcode implements GetPdfItems {

  private final ObjectMapper mapper;

  @Autowired
  public GetPdfItemsHardcode(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Single<List<PdfReceiptItem>> pdfItems(Receipt receipt) {
    return Single.fromCallable(() -> {

      try (final var iterator = mapper.readerFor(PdfReceiptItem.class)
          .<PdfReceiptItem>readValues(HardCode.PDF_RECEIPT_ITEMS)) {
        return iterator.readAll()
            .stream().sorted(ConvertUtil.pdfReceiptItemComparator())
            .collect(Collectors.toCollection(LinkedList::new));
      }
    });
  }

  @Override
  public Single<List<PdfReceiptItem>> pdfItems(Receipt receipt, Building building, List<Apartment> apartments) {
    return pdfItems(receipt);
  }

  @Override
  public void setPlConsumer(Consumer<Consumer<ProgressLayout>> plConsumer) {

  }

  @Override
  public void delete() {

  }
}
