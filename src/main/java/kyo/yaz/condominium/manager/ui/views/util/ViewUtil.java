package kyo.yaz.condominium.manager.ui.views.util;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Span;
import kyo.yaz.condominium.manager.core.domain.Currency;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ViewUtil {

    public static final List<Integer> ITEM_PER_PAGE_OPTIONS = List.of(5, 10, 15, 20, 30, 50, 100);

    private ViewUtil() {
    }

    public static ComboBox<Integer> itemPerPageComboBox() {
        final var itemPerPageComboBox = new ComboBox<>(null, ITEM_PER_PAGE_OPTIONS);
        itemPerPageComboBox.setValue(20);
        return itemPerPageComboBox;
    }

    public static <T> Subscriber<T> subscriber(Runnable onComplete, Consumer<Throwable> errorConsumer) {
        return new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {

            }

            @Override
            public void onNext(T t) {

            }

            @Override
            public void onError(Throwable t) {
                errorConsumer.accept(t);
            }

            @Override
            public void onComplete() {
                onComplete.run();
            }
        };
    }

    public static <T> Subscriber<T> emptySubscriber(Consumer<Throwable> errorConsumer) {
        return new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {

            }

            @Override
            public void onNext(T unused) {

            }

            @Override
            public void onError(Throwable throwable) {
                errorConsumer.accept(throwable);
            }

            @Override
            public void onComplete() {

            }
        };
    }

    public static ComboBox<Integer> yearPicker() {
        return yearPicker(ZoneId.systemDefault());
    }

    public static MultiSelectComboBox<Month> monthMultiComboBox() {
        return monthMultiComboBox(null);
    }

    public static MultiSelectComboBox<Month> monthMultiComboBox(String label) {
        return new MultiSelectComboBox<>(label, Month.values());
    }

    public static ComboBox<Month> monthPicker() {
        return monthPicker(ZoneId.systemDefault());

    }

    public static ComboBox<Month> monthPicker(ZoneId zoneId) {
        LocalDate now = LocalDate.now(zoneId);

        final var comboBox = new ComboBox<>(Labels.MONTH, Month.values());

        comboBox.setValue(now.getMonth());
        comboBox.setAllowCustomValue(false);
        comboBox.setAutoOpen(true);
        return comboBox;
    }

    public static ComboBox<Integer> yearPicker(ZoneId zoneId) {
        LocalDate now = LocalDate.now(zoneId);

        List<Integer> selectableYears = IntStream
                .range(now.getYear() - 3, now.getYear() + 1)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        final var comboBox = new ComboBox<>(Labels.YEAR, selectableYears);

        comboBox.setValue(now.getYear());
        comboBox.setAllowCustomValue(false);
        comboBox.setAutoOpen(true);
        return comboBox;
    }

    public static ComboBox<Currency> currencyComboBox() {
        return currencyComboBox(null);
    }

    public static ComboBox<Currency> currencyComboBox(String label) {
        return currencyComboBox(label, Currency.VED);
    }

    public static ComboBox<Currency> currencyComboBox(String label, Currency defaultValue) {
        return enumComboBox(label, Currency.values, defaultValue);
    }

    public static <T> ComboBox<T> enumComboBox(String label, T[] values) {
        return enumComboBox(label, values, null);
    }

    public static <T> ComboBox<T> enumComboBox(String label, T[] values, T defaultValue) {
        final var comboBox = new ComboBox<>(label, values);
        if (defaultValue != null) {
            comboBox.setValue(defaultValue);
        }
        comboBox.setAllowCustomValue(false);
        comboBox.setAutoOpen(true);
        return comboBox;
    }

    public static DatePicker datePicker(String label) {
        return datePicker(label, ZoneId.systemDefault());
    }

    public static DatePicker datePicker(String label, ZoneId zoneId) {
        DatePicker.DatePickerI18n singleFormatI18n = new DatePicker.DatePickerI18n();
        singleFormatI18n.setDateFormat("yyyy-MM-dd");
        final var datePicker = new DatePicker(label, LocalDate.now(zoneId));
        datePicker.setI18n(singleFormatI18n);

        return datePicker;
    }

    public static Span span(String title, String text) {
        final var span = new Span();
        span.setTitle(title);
        span.setText(text);
        return span;
    }

}
