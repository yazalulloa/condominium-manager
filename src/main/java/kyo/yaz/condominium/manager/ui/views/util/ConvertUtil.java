package kyo.yaz.condominium.manager.ui.views.util;

import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.persistence.domain.ExtraCharge;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.domain.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

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
                building.id(), building.name(), building.rif(), building.reserveFund(), building.reserveFundCurrency(), building.mainCurrency(),
                building.currenciesToShowAmountToPay(), ConvertUtil.toList(building.extraCharges(), ConvertUtil::viewItem));
    }

    public static Building building(BuildingViewItem item) {

        return new Building(item.getId().toUpperCase(), item.getName(), item.getRif(), item.getReserveFund(), item.getReserveFundCurrency(), item.getMainCurrency(),
                item.getCurrenciesToShowAmountToPay(), ConvertUtil.toList(item.getExtraCharges(), ConvertUtil::extraCharge));
    }

    public static ReceiptViewItem receipt(Receipt receipt) {

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
    }

    public static Expense expense(ExpenseViewItem item) {
        return Expense.builder()
                .description(item.getDescription())
                .amount(item.getAmount())
                .currency(item.getCurrency())
                .type(item.getType())
                .build();
    }

    public static Debt debt(DebtViewItem item) {
        return Debt.builder()
                .aptNumber(item.getAptNumber())
                .name(item.getName())
                .receipts(item.getReceipts())
                .amount(item.getAmount())
                .currency(item.getCurrency())
                .months(item.getMonths())
                .previousPaymentAmount(item.getPreviousPaymentAmount())
                .previousPaymentAmountCurrency(item.getPreviousPaymentAmountCurrency())
                .build();
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
}
