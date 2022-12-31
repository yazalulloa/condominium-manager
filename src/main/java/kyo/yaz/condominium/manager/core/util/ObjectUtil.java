package kyo.yaz.condominium.manager.core.util;

import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.persistence.domain.ExtraCharge;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ObjectUtil {

    public static boolean aBoolean(Boolean aBoolean) {
        return Optional.ofNullable(aBoolean).orElse(false);
    }

    public static BigDecimal totalAptPay(BigDecimal unCommonPayPerApt, boolean fixedPay, BigDecimal fixedPayAmount,
                                         Currency currency, BigDecimal rate,
                                         BigDecimal totalCommonExpenses,
                                         BigDecimal aptAliquot,
                                         Collection<ExtraCharge> extraCharges) {
        if (fixedPay) {

            return totalPayment(fixedPayAmount, currency, rate, extraCharges)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {

            //document.add(new Paragraph("MONTO DE GASTOS NO COMUNES POR C/U: " + currencyType.numberFormat().format(unCommonPay)));

            final var aliquotAmount = DecimalUtil.percentageOf(aptAliquot, totalCommonExpenses);
            // document.add(new Paragraph("MONTO POR ALIQUOTA: " + currencyType.numberFormat().format(aliquotAmount)));
            final var beforePay = aliquotAmount.add(unCommonPayPerApt);//.setScale(2, RoundingMode.UP);
            return totalPayment(beforePay, currency, rate, extraCharges).setScale(2, RoundingMode.HALF_UP);

        }
    }

    public static BigDecimal totalPayment(BigDecimal payment, Currency currencyType, BigDecimal usdExchangeRate, Collection<ExtraCharge> extraCharges) {
        //final var usdExchangeRate = BigDecimal.valueOf(rate);


        if (!extraCharges.isEmpty()) {
            final var vesExtraCharge = extraCharges.stream().filter(c -> c.currency() == Currency.VED).map(ExtraCharge::amount)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);

            final var usdExtraCharge = extraCharges.stream().filter(c -> c.currency() == Currency.USD).map(ExtraCharge::amount)

                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            if (currencyType == Currency.USD) {
                payment = payment.add(usdExtraCharge);

                if (!DecimalUtil.equalsToZero(vesExtraCharge)) {
                    final var toUsd = vesExtraCharge.divide(usdExchangeRate, 2, RoundingMode.HALF_UP);
                    payment = payment.add(toUsd);
                }
            } else {
                payment = payment.add(vesExtraCharge);
                if (!DecimalUtil.equalsToZero(usdExtraCharge)) {
                    final var toVes = usdExtraCharge.multiply(usdExchangeRate, MathContext.UNLIMITED);//.setScale(2, RoundingMode.UP);
                    payment = payment.add(toVes);
                }
            }
        }


        return payment;
    }

    public static List<ExtraCharge> extraCharges(String aptNumber, List<ExtraCharge> first, List<ExtraCharge> second) {

        final var receiptCharges = Optional.ofNullable(first)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(extraCharge -> extraCharge.aptNumber().equals(aptNumber))
                .filter(extraCharge -> DecimalUtil.greaterThanZero(extraCharge.amount()));

        final var buildingCharges = Optional.ofNullable(second)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(extraCharge -> extraCharge.aptNumber().equals(aptNumber))
                .filter(extraCharge -> DecimalUtil.greaterThanZero(extraCharge.amount()));

        return Stream.concat(receiptCharges, buildingCharges).toList();
    }
}
