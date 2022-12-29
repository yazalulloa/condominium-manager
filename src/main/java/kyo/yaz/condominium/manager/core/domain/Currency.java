package kyo.yaz.condominium.manager.core.domain;

import kyo.yaz.condominium.manager.ui.views.util.ConvertUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

public enum Currency {


    USD, VED;

    public static final Currency[] values = values();

    public static BigDecimal toCurrency(BigDecimal amount, Currency currencyAmount, BigDecimal rate, Currency currencyRate) {
        if (currencyAmount == currencyRate) {
            return amount;
        }

        if (currencyRate == USD) {
            return amount.divide(rate, 2, RoundingMode.HALF_UP);
        } else {
            return amount.multiply(rate);
        }

    }

    public NumberFormat numberFormat() {
        if (this == Currency.USD) {
            return ConvertUtil.US_FORMAT;
        }
        return ConvertUtil.VE_FORMAT;
    }
}
