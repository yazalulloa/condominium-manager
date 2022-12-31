package kyo.yaz.condominium.manager.ui.views.util;

import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.core.util.ObjectUtil;
import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.persistence.domain.ExtraCharge;
import kyo.yaz.condominium.manager.persistence.domain.IAmountCurrency;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.domain.*;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

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

    public static ApartmentViewItem viewItem(Apartment apartment) {

        final var optional = Optional.ofNullable(apartment.apartmentId());
        return new ApartmentViewItem(
                optional.map(Apartment.ApartmentId::buildingId).orElse(null),
                optional.map(Apartment.ApartmentId::number).orElse(null), apartment.name(), apartment.idDoc(), apartment.emails(),
                apartment.paymentType(), apartment.amountToPay());
    }

    public static Apartment apartment(ApartmentViewItem viewItem) {

        final var apartmentId = new Apartment.ApartmentId(viewItem.getBuildingId(), viewItem.getNumber());
        return new Apartment(apartmentId, viewItem.getName(), viewItem.getIdDoc(), viewItem.getEmails(), viewItem.getPaymentType(), viewItem.getAmountToPay());
    }


    public static BuildingViewItem viewItem(Building building) {


        return new BuildingViewItem(
                building.id(), building.name(), building.rif(), building.reserveFund(), building.reserveFundCurrency(), building.mainCurrency(), building.debtCurrency(),
                building.currenciesToShowAmountToPay(), ConvertUtil.toList(building.extraCharges(), ConvertUtil::viewItem), ObjectUtil.aBoolean(building.fixedPay()),
                building.fixedPayAmount());
    }

    public static Building building(BuildingViewItem item) {

        return new Building(item.getId().toUpperCase(), item.getName(), item.getRif(), item.getReserveFund(), item.getReserveFundCurrency(), item.getMainCurrency(), item.getDebtCurrency(),
                item.getCurrenciesToShowAmountToPay(), ConvertUtil.toList(item.getExtraCharges(), ConvertUtil::extraCharge), item.isFixedPay(), item.getFixedPayAmount());
    }

   /* public static ReceiptViewItem receipt(Receipt receipt) {

        final var createdAt = Optional.ofNullable(receipt.createdAt())
                .map(zonedDateTime -> zonedDateTime.withZoneSameInstant(DateUtil.VE_ZONE))
                .map(ZonedDateTime::toString)
                .orElse(null);

        return ReceiptViewItem.builder()
                .id(receipt.id())
                .buildingId(receipt.buildingId())
                .date(receipt.date())
                .createdAt(createdAt)
                .build();
    }*/

    public static ReceiptFormItem formItem(Receipt receipt) {
        return new ReceiptFormItem(receipt.buildingId(), receipt.year(), receipt.month(), receipt.rate(), receipt.date());
    }

    public static Expense expense(ExpenseViewItem item) {
        return Expense.builder()
                .description(item.getDescription())
                .amount(item.getAmount())
                .currency(item.getCurrency())
                .type(item.getType())
                .build();
    }

    public static ExpenseViewItem viewItem(Expense expense) {
        return new ExpenseViewItem(expense.description(), expense.amount(), expense.currency(), expense.type());
    }

    public static Debt debt(DebtViewItem item) {
        return Debt.builder()
                .aptNumber(item.getAptNumber())
                .name(item.getName())
                .receipts(item.getReceipts())
                .amount(item.getAmount())
                .months(item.getMonths())
                .previousPaymentAmount(item.getPreviousPaymentAmount())
                .previousPaymentAmountCurrency(item.getPreviousPaymentAmountCurrency())
                .build();
    }

    public static DebtViewItem viewItem(Debt item) {
        return new DebtViewItem(item.aptNumber(), item.name(), item.receipts(), item.amount(), item.months(), item.previousPaymentAmount(), item.previousPaymentAmountCurrency());
    }


    public static ExtraCharge extraCharge(ExtraChargeViewItem viewItem) {
        return new ExtraCharge(viewItem.getAptNumber(), viewItem.getDescription(), viewItem.getAmount(), viewItem.getCurrency());
    }

    public static ExtraChargeViewItem viewItem(ExtraCharge extraCharge) {
        return new ExtraChargeViewItem(extraCharge.aptNumber(), extraCharge.description(), extraCharge.amount(), extraCharge.currency());
    }

    public static <T, S> List<T> toList(Collection<S> collection, Function<S, T> function) {

        if (collection == null) {
            return Collections.emptyList();
        }

        return collection.stream().map(function).toList();
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
