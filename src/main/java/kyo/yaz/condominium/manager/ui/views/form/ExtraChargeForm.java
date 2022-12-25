package kyo.yaz.condominium.manager.ui.views.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.ui.views.base.AbstractView;
import kyo.yaz.condominium.manager.ui.views.domain.ExtraChargeViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.Collection;

@Slf4j
public class ExtraChargeForm extends FormLayout implements AbstractView {
    @PropertyId("aptNumber")
    private final ComboBox<String> aptNumberComboBox = new ComboBox<>(Labels.ExtraCharge.APT_LABEL);

    @PropertyId("description")
    private final TextField descriptionField = new TextField(Labels.ExtraCharge.DESCRIPTION_LABEL);

    @PropertyId("amount")
    private final BigDecimalField amountField = new BigDecimalField(Labels.ExtraCharge.AMOUNT_LABEL);

    @PropertyId("currency")
    private final ComboBox<Currency> currencyComboBox = new ComboBox<>(Labels.ExtraCharge.CURRENCY_LABEL, Currency.values);

    private final Button addBtn = new Button(Labels.ADD);
    private final Button cancelBtn = new Button(Labels.CANCEL);
    private final Binder<ExtraChargeViewItem> binder = new BeanValidationBinder<>(ExtraChargeViewItem.class);

    ExtraChargeViewItem extraCharge;

    public ExtraChargeForm(Collection<String> apartments) {
        super();
        addClassName("extra-charge-form");

        currencyComboBox.setItemLabelGenerator(Currency::name);
        aptNumberComboBox.setAllowCustomValue(false);
        currencyComboBox.setAllowCustomValue(false);

        add(
                aptNumberComboBox,
                descriptionField,
                amountField,
                currencyComboBox,
                createButtonsLayout());

        binder.bindInstanceFields(this);
        setExtraCharge(new ExtraChargeViewItem());

        aptNumberComboBox.setItems(apartments);
    }

    @Override
    public Component component() {
        return this;
    }

    @Override
    public Logger logger() {
        return log;
    }

    private HorizontalLayout createButtonsLayout() {
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        addBtn.addClickShortcut(Key.ENTER);
        cancelBtn.addClickShortcut(Key.ESCAPE);

        addBtn.addClickListener(event -> validateAndSave());
        cancelBtn.addClickListener(event -> {
            binder.readBean(null);
        });


        binder.addStatusChangeListener(e -> addBtn.setEnabled(binder.isValid()));

        return new HorizontalLayout(addBtn, cancelBtn);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(extraCharge);
            fireEvent(new ExtraChargeForm.SaveEvent(this, extraCharge));
            setExtraCharge(new ExtraChargeViewItem());
        } catch (ValidationException e) {
            log.error("ERROR_VALIDATING", e);
            asyncNotification(e.getMessage());

        }
    }

    public void setExtraCharge(ExtraChargeViewItem viewItem) {
        this.extraCharge = viewItem;
        binder.readBean(viewItem);

    }

    public static abstract class FormEvent extends ComponentEvent<ExtraChargeForm> {
        private final ExtraChargeViewItem obj;

        protected FormEvent(ExtraChargeForm source, ExtraChargeViewItem obj) {
            super(source, false);
            this.obj = obj;
        }

        public ExtraChargeViewItem getObj() {
            return obj;
        }
    }

    public static class SaveEvent extends FormEvent {
        SaveEvent(ExtraChargeForm source, ExtraChargeViewItem obj) {
            super(source, obj);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

}
