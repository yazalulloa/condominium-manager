package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import kyo.yaz.condominium.manager.core.domain.BcvUsdRateResult;
import kyo.yaz.condominium.manager.core.service.entity.RateService;
import kyo.yaz.condominium.manager.core.service.entity.SequenceService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.core.vertx.VertxHandler;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SaveNewBcvRate {

    private final SequenceService sequenceService;
    private final RateService rateService;
    //private final GetBcvUsdRate getBcvUsdRate;
    private final BcvGetDocumentQueue bcvGetDocumentQueue;
    private final NotificationService notificationService;
    private final VertxHandler vertxHandler;

    private Single<BcvUsdRateResult> getNewRate() {
        return vertxHandler.single(bcvGetDocumentQueue::getNewRate);
    }

    public Single<BcvUsdRateResult> saveNewRate() {

        return getNewRate()
                .flatMap(result -> {


                    if (result.state() != BcvUsdRateResult.State.NEW_RATE) {
                        return Single.just(result);
                    }

                    final var rate = result.rate();

                    final var newRateSingle = sequenceService.nextSequence(Sequence.Type.RATES)
                            .<Rate>map(id -> rate.toBuilder()
                                    .id(id)
                                    .createdAt(DateUtil.nowZonedWithUTC())
                                    .build())
                            .flatMap(rateService::save)
                            .map(r -> new BcvUsdRateResult(BcvUsdRateResult.State.NEW_RATE, r))
                            .cache();

                    final var saveRate = newRateSingle
                            .map(BcvUsdRateResult::rate)
                            .map(r -> "Nueva tasa añadida\n%s\nFecha de la tasa: %s".formatted(r.rate(), r.dateOfRate()))
                            .flatMapCompletable(notificationService::sendNewRate)
                            .andThen(newRateSingle);


                    final var lastSingle = rateService.last(rate.fromCurrency(), rate.toCurrency())
                            .switchIfEmpty(Maybe.fromAction(() -> log.info("LAST RATE NOT FOUND")))
                            .map(lastRate -> {
                                        final var isSameRate = DecimalUtil.equalsTo(lastRate.rate(), rate.rate())
                                                && lastRate.dateOfRate().isEqual(rate.dateOfRate())
                                                && lastRate.source() == rate.source();

                                        if (!isSameRate) {
                                            log.info("LAST RATE IS DIFFERENT \nOLD: {}\nNEW: {}", Json.encodePrettily(lastRate), Json.encodePrettily(rate));
                                            return new BcvUsdRateResult(BcvUsdRateResult.State.NEW_RATE);
                                        }

                                        return new BcvUsdRateResult(BcvUsdRateResult.State.SAME_RATE);
                                    }
                            )
                            .defaultIfEmpty(new BcvUsdRateResult(BcvUsdRateResult.State.RATE_NOT_IN_DB));

                    final var existSingle = rateService.exists(rate.hash());

                    return Single.zip(lastSingle, existSingle, (lastResult, exists) -> {

                        if (lastResult.state() == BcvUsdRateResult.State.SAME_RATE) {
                            return Single.just(lastResult);
                        }

                        if (exists) {
                            return Single.just(new BcvUsdRateResult(BcvUsdRateResult.State.HASH_SAVED));
                        }

                        return saveRate;
                    }).flatMap(s -> s);
                });

    }
}
