package kyo.yaz.condominium.manager.core.service.csv;

import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.core.service.entity.RateService;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.stream.Collectors;

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

            final var csvReceipt = new ParseCsv().csvReceipt(inputStream);

            final var buildingMono = buildingService.get(buildingId);

            final var rateMono = rateService.getLast(Currency.USD, Currency.VED);

            return Single.zip(buildingMono, rateMono, (building, rate) -> {

                final var debtList = csvReceipt.debts().stream()
                        .map(debt -> debt.toBuilder()
                                .previousPaymentAmountCurrency(debt.previousPaymentAmount() != null ? building.mainCurrency() : null)
                                .build())
                        .collect(Collectors.toList());

                return Receipt.builder()
                        .buildingId(buildingId)
                        .year(LocalDate.now().getYear())
                        .month(LocalDate.now().getMonth())
                        .date(LocalDate.now())
                        .expenses(csvReceipt.expenses())
                        .debts(debtList)
                        .extraCharges(csvReceipt.extraCharges())
                        .rate(rate)
                        .build();
            });
        });

    }

}
