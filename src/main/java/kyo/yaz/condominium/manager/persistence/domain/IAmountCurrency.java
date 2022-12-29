package kyo.yaz.condominium.manager.persistence.domain;

import kyo.yaz.condominium.manager.core.domain.Currency;

import java.math.BigDecimal;

public interface IAmountCurrency {

    BigDecimal amount();
    Currency currency();
}
