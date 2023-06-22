package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import kyo.yaz.condominium.manager.core.service.entity.RateService;
import kyo.yaz.condominium.manager.core.service.entity.SequenceService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
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
  private final GetBcvUsdRate getBcvUsdRate;
  private final NotificationService notificationService;


  public Single<Boolean> saveNewRate() {

   /* final var dayOfWeek = LocalDate.now().getDayOfWeek();
    if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
      return Single.just(false);
    }*/

    return getBcvUsdRate.newRate()
        .flatMap(rate -> {

          final var saveRate = sequenceService.nextSequence(Sequence.Type.RATES)
              .<Rate>map(id -> rate.toBuilder()
                  .id(id)
                  .createdAt(DateUtil.nowZonedWithUTC())
                  .build())
              .flatMap(rateService::save)
              .map(r -> "Nueva tasa añadida %s %s".formatted(r.rate(), r.dateOfRate()))
              .flatMapCompletable(notificationService::sendNewRate)
              .toSingleDefault(true);

          return rateService.last(rate.fromCurrency(), rate.toCurrency())
              .filter(lastRate -> {
                    final var isSameRate = DecimalUtil.equalsTo(lastRate.rate(), rate.rate())
                        && lastRate.dateOfRate().isEqual(rate.dateOfRate())
                        && lastRate.source() == rate.source();

                    if (!isSameRate) {
                      log.info("LAST RATE IS DIFFERENT \nOLD: {}\nNEW: {}", Json.encodePrettily(lastRate),
                          Json.encodePrettily(rate));
                    }

                    return isSameRate;
                  }
              )
              .map(r -> false)
              .switchIfEmpty(Maybe.fromAction(() -> log.info("LAST RATE NOT FOUND")))
              .switchIfEmpty(saveRate);

        });

  }
}
