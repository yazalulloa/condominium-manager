package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
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

    private Maybe<Rate> getNewRate() {
        return vertxHandler.maybe(bcvGetDocumentQueue::getNewRate);
    }

    public Single<Boolean> saveNewRate() {

        return getNewRate()
                .flatMapSingle(rate -> {

                    final var saveRate = sequenceService.nextSequence(Sequence.Type.RATES)
                            .<Rate>map(id -> rate.toBuilder()
                                    .id(id)
                                    .createdAt(DateUtil.nowZonedWithUTC())
                                    .build())
                            .flatMap(rateService::save)
                            .map(r -> "Nueva tasa añadida\n%s\nFecha de la tasa: %s".formatted(r.rate(), r.dateOfRate()))
                            .flatMapCompletable(notificationService::sendNewRate)
                            .toSingleDefault(true);


                    final var lastSingle = rateService.last(rate.fromCurrency(), rate.toCurrency())
                            .switchIfEmpty(Maybe.fromAction(() -> log.info("LAST RATE NOT FOUND")))
                            .map(lastRate -> {
                                        final var isSameRate = DecimalUtil.equalsTo(lastRate.rate(), rate.rate())
                                                && lastRate.dateOfRate().isEqual(rate.dateOfRate())
                                                && lastRate.source() == rate.source();

                                        if (!isSameRate) {
                                            //log.info("LAST RATE IS DIFFERENT \nOLD: {}\nNEW: {}", Json.encodePrettily(lastRate), Json.encodePrettily(rate));
                                        }

                                        return isSameRate;
                                    }
                            )
                            .defaultIfEmpty(false);

                    final var existSingle = rateService.exists(rate.hash());

                    return Single.zip(lastSingle, existSingle, (lastIsSame, exists) -> {

                        if (lastIsSame || exists) {
                            if (!lastIsSame) {
                                //log.info("HASH IS ALREADY SAVED");
                            }

                            return Single.just(false);
                        }

                        return saveRate;
                    }).flatMap(s -> s);
                })
                .defaultIfEmpty(false);

    }
}
