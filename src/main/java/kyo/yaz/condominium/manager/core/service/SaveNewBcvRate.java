package kyo.yaz.condominium.manager.core.service;

import kyo.yaz.condominium.manager.core.service.entity.RateService;
import kyo.yaz.condominium.manager.core.service.entity.SequenceService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class SaveNewBcvRate {

    private final SequenceService sequenceService;

    private final RateService rateService;
    private final GetBcvUsdRate getBcvUsdRate;

    @Autowired
    public SaveNewBcvRate(SequenceService sequenceService, RateService rateService, GetBcvUsdRate getBcvUsdRate) {
        this.sequenceService = sequenceService;
        this.rateService = rateService;
        this.getBcvUsdRate = getBcvUsdRate;
    }


    public Mono<Boolean> saveNewRate() {

        return getBcvUsdRate.newRate()
                .flatMap(rate -> {

                    final var saveRate = sequenceService.nextSequence(Sequence.Type.RATES)
                            .<Rate>map(id -> rate.toBuilder()
                                    .id(id)
                                    .createdAt(DateUtil.nowZonedWithUTC())
                                    .build())
                            .flatMap(rateService::save)
                            .map(o -> true);

                    return rateService.last(rate.fromCurrency(), rate.toCurrency())
                            .filter(r -> DecimalUtil.equalsTo(r.rate(), rate.rate())
                                    && Objects.equals(r.dateOfRate(), rate.dateOfRate()))
                            .map(r -> false)
                            .switchIfEmpty(saveRate);

                });

    }
}
