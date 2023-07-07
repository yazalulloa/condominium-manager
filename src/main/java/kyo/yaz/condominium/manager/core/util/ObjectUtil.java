package kyo.yaz.condominium.manager.core.util;

import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.persistence.domain.ExtraCharge;
import kyo.yaz.condominium.manager.persistence.entity.Building;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class ObjectUtil {

    public static boolean aBoolean(Boolean aBoolean) {
        return Optional.ofNullable(aBoolean).orElse(false);
    }

    public static Map<Currency, BigDecimal> totalAptPay(BigDecimal unCommonPayPerApt, Building building, BigDecimal rate,
                                                        BigDecimal totalCommonExpenses,
                                                        BigDecimal aptAliquot,
                                                        Collection<ExtraCharge> extraCharges) {

        final var currency = building.mainCurrency();
        if (ObjectUtil.aBoolean(building.fixedPay())) {

            return totalPayment(building.fixedPay(), building.fixedPayAmount(), currency, rate, extraCharges);

        } else {

            //document.add(new Paragraph("MONTO DE GASTOS NO COMUNES POR C/U: " + currencyType.numberFormat().format(unCommonPay)));

            final var aliquotAmount = DecimalUtil.percentageOf(aptAliquot, totalCommonExpenses);
            // document.add(new Paragraph("MONTO POR ALIQUOTA: " + currencyType.numberFormat().format(aliquotAmount)));
            final var beforePay = aliquotAmount.add(unCommonPayPerApt);//.setScale(2, RoundingMode.UP);
            return totalPayment(building.fixedPay(), beforePay, currency, rate, extraCharges);

        }
    }

    public static Map<Currency, BigDecimal> totalPayment(boolean fixedPay,
                                                         BigDecimal preCalculatedPayment,
                                                         Currency currencyType, BigDecimal usdExchangeRate,
                                                         Collection<ExtraCharge> extraCharges) {

        var usdPay = BigDecimal.ZERO;
        var vesPay = BigDecimal.ZERO;

        Function<BigDecimal, BigDecimal> toUsd = ves -> {
            return ves.divide(usdExchangeRate, 2, RoundingMode.HALF_UP);
        };

        Function<BigDecimal, BigDecimal> toVes = usd -> {
            return usd.multiply(usdExchangeRate);
        };

        final var vesExtraCharge = extraCharges.stream().filter(c -> c.currency() == Currency.VED)
                .map(ExtraCharge::amount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        final var usdExtraCharge = extraCharges.stream().filter(c -> c.currency() == Currency.USD)
                .map(ExtraCharge::amount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        vesPay = vesPay.add(vesExtraCharge).add(DecimalUtil.equalsToZero(usdExtraCharge) ? BigDecimal.ZERO : toVes.apply(usdExtraCharge));
        usdPay = usdPay.add(usdExtraCharge).add(DecimalUtil.equalsToZero(vesExtraCharge) ? BigDecimal.ZERO : toUsd.apply(vesExtraCharge));


        if (fixedPay) {
            if (currencyType == Currency.USD) {
                usdPay = usdPay.add(preCalculatedPayment);
                vesPay = vesPay.add(toVes.apply(preCalculatedPayment));
            } else {
                vesPay = vesPay.add(preCalculatedPayment);
                usdPay = usdPay.add(toUsd.apply(preCalculatedPayment));
            }
        } else {
            vesPay = vesPay.add(preCalculatedPayment);
            usdPay = usdPay.add(toUsd.apply(preCalculatedPayment));
        }

        Function<BigDecimal, BigDecimal> function = bigDecimal -> {
            /*if (building.roundUpPayments()) {
                return bigDecimal.setScale(0, RoundingMode.UP);
            }*/

            return bigDecimal.setScale(2, RoundingMode.HALF_UP);
        };

        return Map.of(
                Currency.USD, function.apply(usdPay),
                Currency.VED, function.apply(vesPay)
        );

       /* if (!extraCharges.isEmpty()) {


            if (fixedPay) {
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
            } else {
                payment = payment.add(vesExtraCharge);
                if (!DecimalUtil.equalsToZero(usdExtraCharge)) {
                    final var toVes = usdExtraCharge.multiply(usdExchangeRate, MathContext.UNLIMITED);//.setScale(2, RoundingMode.UP);
                    payment = payment.add(toVes);
                }

            }


        }


        return payment;*/
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
