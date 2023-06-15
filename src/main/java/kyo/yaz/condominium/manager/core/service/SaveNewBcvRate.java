package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.service.entity.RateService;
import kyo.yaz.condominium.manager.core.service.entity.SequenceService;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveNewBcvRate {

  private final SequenceService sequenceService;
  private final RateService rateService;
  private final GetBcvUsdRate getBcvUsdRate;
  private final NotificationService notificationService;


  public Single<Boolean> saveNewRate() {

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
              .filter(r ->
                  DecimalUtil.equalsTo(r.rate(), rate.rate())
                      && r.dateOfRate().isEqual(rate.dateOfRate())
                      && r.source() == rate.source()
              )
              .map(r -> false)
              .switchIfEmpty(saveRate);

        });

  }
}
