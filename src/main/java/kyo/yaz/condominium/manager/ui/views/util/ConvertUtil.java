package kyo.yaz.condominium.manager.ui.views.util;

import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.persistence.domain.IAmountCurrency;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.domain.ReceiptFormItem;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConvertUtil {
    public static final NumberFormat VE_FORMAT;
    public static final NumberFormat US_FORMAT;

    static {

        final var veSymbols = new DecimalFormatSymbols(new Locale("es", "VE"));
        veSymbols.setCurrencySymbol("Bs. ");
        VE_FORMAT = new DecimalFormat("¤#,##0.00;¤-#,##0.00", veSymbols);

        //veFormat = DecimalFormat.getCurrencyInstance(locale);
        final var usSymbols = new DecimalFormatSymbols(Locale.US);
        usSymbols.setCurrencySymbol("$ ");
        //"¤#,##0.00"
        US_FORMAT = new DecimalFormat("¤#,##0.00;¤-#,##0.00", usSymbols);
        //usFormat = DecimalFormat.getCurrencyInstance(Locale.US);
    }


    public static ReceiptFormItem formItem(Receipt receipt) {
        return new ReceiptFormItem(receipt.buildingId(), receipt.year(), receipt.month(), receipt.rate(), receipt.date());
    }

    public static <T, S> List<T> toList(Collection<S> collection, Function<S, T> function) {

        if (collection == null) {
            return Collections.emptyList();
        }

        return collection.stream().map(function).collect(Collectors.toCollection(LinkedList::new));
    }

    public static String format(BigDecimal amount, Currency currency) {
        final var numberFormat = Optional.ofNullable(currency)
                .orElse(Currency.VED)
                .numberFormat();

        final var decimal = Optional.ofNullable(amount)
                .orElse(BigDecimal.ZERO);

        return numberFormat.format(decimal);
    }

    public static <T extends IAmountCurrency> Pair<BigDecimal, Currency> pair(Collection<T> collection, BigDecimal usdRate) {
        return pair(collection, r -> true, usdRate);
    }

    public static <T extends IAmountCurrency> Pair<BigDecimal, Currency> pair(Collection<T> collection, Predicate<T> predicate, BigDecimal usdRate) {


        final var usdAmount = collection.stream().filter(predicate)
                .filter(o -> o.currency() == Currency.USD)
                .map(IAmountCurrency::amount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        final var vedAmount = collection.stream().filter(predicate)
                .filter(o -> o.currency() == Currency.VED)
                .map(IAmountCurrency::amount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);


        if (DecimalUtil.greaterThanZero(vedAmount)) {
            final var amount = usdAmount.multiply(usdRate)
                    .add(vedAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            return Pair.of(amount, Currency.VED);
        }

        return Pair.of(usdAmount, Currency.USD);
    }
}
