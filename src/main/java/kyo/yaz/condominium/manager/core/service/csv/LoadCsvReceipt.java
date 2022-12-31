package kyo.yaz.condominium.manager.core.service.csv;

import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.core.service.entity.RateService;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

@Service
public class LoadCsvReceipt {

    private final BuildingService buildingService;
    private final RateService rateService;

    @Autowired
    public LoadCsvReceipt(BuildingService buildingService, RateService rateService) {
        this.buildingService = buildingService;
        this.rateService = rateService;
    }

    public Single<Receipt> load(String buildingId, InputStream inputStream) {
        return Single.defer(() -> {

            try (final var workbook = new XSSFWorkbook(inputStream)) {

                final var numberOfSheets = workbook.getNumberOfSheets();

                final var expensesSheet = workbook.getSheetAt(0);
                final var debtsSheet = workbook.getSheetAt(1);
                final var reserveFundSheet = workbook.getSheetAt(3);

                final var extraChargesSheet = numberOfSheets > 4 ? workbook.getSheetAt(4) : null;

                final var parseCsv = new ParseCsv();

                final var expenses = parseCsv.expenses(expensesSheet);
                final var debts = parseCsv.debts(debtsSheet);
                final var extraCharges = Optional.ofNullable(extraChargesSheet)
                        .map(parseCsv::extraCharges)
                        .orElseGet(Collections::emptyList);


                final var buildingMono = buildingService.get(buildingId);

                final var rateMono = rateService.getLast(Currency.USD, Currency.VED);

                return Single.zip(buildingMono, rateMono, (building, rate) -> {


                    return Receipt.builder()
                            .buildingId(buildingId)
                            .year(LocalDate.now().getYear())
                            .month(LocalDate.now().getMonth())
                            .date(LocalDate.now())
                            .expenses(expenses)
                            .debts(debts)
                            .extraCharges(extraCharges)
                            .rate(rate)
                            .build();
                });
            }
        });

    }
}
