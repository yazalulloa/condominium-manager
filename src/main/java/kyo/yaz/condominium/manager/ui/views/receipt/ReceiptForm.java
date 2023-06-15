package kyo.yaz.condominium.manager.ui.views.receipt;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.core.service.entity.RateService;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import kyo.yaz.condominium.manager.ui.views.actions.ViewEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Month;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
    private ReceiptFormItem item;
    private final RateService rateService;

    private final TranslationProvider translationProvider;

    @Autowired
    public ReceiptForm(TranslationProvider translationProvider, RateService rateService) {
        this.translationProvider = translationProvider;
        this.rateService = rateService;
    }

    public void init() {
        monthPicker.setItemLabelGenerator(m -> translationProvider.translate(m.name()));
        addClassName("receipt-form");

        rateComboBox.setItems(rateService::stream);

        rateComboBox.setItemLabelGenerator(rate -> rate.rate() + " " + rate.dateOfRate() + " " + rate.source() + " " + rate.fromCurrency());

        add(buildingComboBox, yearPicker, monthPicker, datePicker, rateComboBox);

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

    public static class SaveEvent extends ViewEvent<ReceiptForm, ReceiptFormItem> {
        SaveEvent(ReceiptForm source, ReceiptFormItem obj) {
            super(source, obj);
        }
    }
}
