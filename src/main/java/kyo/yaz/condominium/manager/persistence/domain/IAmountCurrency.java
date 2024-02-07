package kyo.yaz.condominium.manager.persistence.domain;

import java.math.BigDecimal;
import kyo.yaz.condominium.manager.core.domain.Currency;

public interface IAmountCurrency {

  BigDecimal amount();

  Currency currency();
}
