package kyo.yaz.condominium.manager.ui.views.receipt;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import kyo.yaz.condominium.manager.core.service.entity.RateService;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.ui.views.actions.FormEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.domain.ReceiptFormItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;

import java.time.Month;


public class ReceiptForm extends BaseForm {

    @PropertyId("buildingId")
    private final ComboBox<String> buildingComboBox = new ComboBox<>(Labels.Receipt.BUILDING_LABEL);
    @PropertyId("year")
    private final ComboBox<Integer> yearPicker = ViewUtil.yearPicker();
    @PropertyId("month")
    private final ComboBox<Month> monthPicker = ViewUtil.monthPicker();
    @PropertyId("rate")
    private final ComboBox<Rate> rateComboBox = new ComboBox<>(Labels.Receipt.RATE_LABEL);

    @PropertyId("date")
    private final DatePicker datePicker = ViewUtil.datePicker(Labels.Receipt.RECEIPT_DATE_LABEL);
    private final Binder<ReceiptFormItem> binder = new BeanValidationBinder<>(ReceiptFormItem.class);
    private final RateService rateService;
    private ReceiptFormItem item;

    public ReceiptForm(RateService rateService) {
        super();
        this.rateService = rateService;
        init();
    }

    private void init() {
        addClassName("receipt-form");

        rateComboBox.setItems(rateService::stream);

        rateComboBox.setItemLabelGenerator(rate -> rate.rate() + " " + rate.dateOfRate() + " " + rate.source() + " " + rate.fromCurrency());

        final var horizontalLayout = new HorizontalLayout(buildingComboBox, yearPicker, monthPicker, datePicker, rateComboBox);

        add(horizontalLayout);

        binder.bindInstanceFields(this);

        final var formItem = new ReceiptFormItem();
        formItem.setBuildingId(buildingComboBox.getValue());
        formItem.setYear(yearPicker.getValue());
        formItem.setMonth(monthPicker.getValue());
        formItem.setDate(datePicker.getValue());
        setItem(formItem);
    }


    public Binder<ReceiptFormItem> binder() {
        return binder;
    }

    public ComboBox<String> buildingComboBox() {
        return buildingComboBox;
    }

    public void setItem(ReceiptFormItem item) {
        this.item = item;
        binder.readBean(item);
    }


    public void validateAndSave() {
        try {
            binder.writeBean(item);
            fireEvent(new SaveEvent(this, item));
            setItem(new ReceiptFormItem());
        } catch (ValidationException e) {
            logger().error("ERROR_VALIDATING", e);
            asyncNotification(e.getMessage());

        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    public static class SaveEvent extends FormEvent<ReceiptForm, ReceiptFormItem> {
        SaveEvent(ReceiptForm source, ReceiptFormItem obj) {
            super(source, obj);
        }
    }
}
