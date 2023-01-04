package kyo.yaz.condominium.manager.persistence.repository.base;

import kyo.yaz.condominium.manager.persistence.domain.request.RateQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Rate;

import java.util.stream.Stream;

public interface RateBlockingCustomRepository {

    Stream<Rate> stream(RateQueryRequest request);
}
