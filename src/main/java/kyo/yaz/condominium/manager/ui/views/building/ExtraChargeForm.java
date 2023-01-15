package kyo.yaz.condominium.manager.ui.views.building;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.ui.views.actions.FormEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.domain.ExtraChargeViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;

import java.math.BigDecimal;
import java.util.Collection;


public class ExtraChargeForm extends BaseForm {
    @PropertyId("aptNumber")
    private final ComboBox<String> aptNumberComboBox = new ComboBox<>(Labels.ExtraCharge.APT_LABEL);

    @PropertyId("description")
    private final TextField descriptionField = new TextField(Labels.ExtraCharge.DESCRIPTION_LABEL);

    @PropertyId("amount")
    private final BigDecimalField amountField = new BigDecimalField(Labels.ExtraCharge.AMOUNT_LABEL);

    @PropertyId("currency")
    private final ComboBox<Currency> currencyComboBox = ViewUtil.currencyComboBox(Labels.ExtraCharge.CURRENCY_LABEL);


    private final Binder<ExtraChargeViewItem> binder = new BeanValidationBinder<>(ExtraChargeViewItem.class);

    ExtraChargeViewItem item;


    public ExtraChargeForm() {
        super();
        addClassName("extra-charge-form");


        aptNumberComboBox.setAllowCustomValue(false);

        add(
                aptNumberComboBox,
                descriptionField,
                amountField,
                currencyComboBox,
                createButtonsLayout());

        binder.bindInstanceFields(this);
        setItem(new ExtraChargeViewItem());
    }

    public void setApartments(Collection<String> apartments) {
        aptNumberComboBox.setItems(apartments);
    }

    public ExtraChargeViewItem defaultItem() {
        return ExtraChargeViewItem.builder()
                .amount(BigDecimal.ZERO)
                .currency(Currency.VED)
                .build();
    }


    private HorizontalLayout createButtonsLayout() {

        final var addBtn = new Button(Labels.SAVE);
        final var deleteBtn = new Button(Labels.DELETE);
        final var cancelBtn = new Button(Labels.CANCEL);

        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        addBtn.addClickShortcut(Key.ENTER);
        cancelBtn.addClickShortcut(Key.ESCAPE);


        addBtn.addClickListener(event -> validateAndSave());
        deleteBtn.addClickListener(event -> fireEvent(new DeleteEvent(this, item)));
        cancelBtn.addClickListener(event -> fireEvent(new CloseEvent(this)));


        binder.addStatusChangeListener(e -> addBtn.setEnabled(binder.isValid()));

        return new HorizontalLayout(addBtn, deleteBtn, cancelBtn);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(item);
            fireEvent(new ExtraChargeForm.SaveEvent(this, item));
            setItem(new ExtraChargeViewItem());
        } catch (ValidationException e) {
            logger().error("ERROR_VALIDATING", e);
            asyncNotification(e.getMessage());

        }
    }

    public void setItem(ExtraChargeViewItem viewItem) {
        this.item = viewItem;
        binder.readBean(viewItem);

    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    private static abstract class ExtraChargeFormEvent extends FormEvent<ExtraChargeForm, ExtraChargeViewItem> {

        protected ExtraChargeFormEvent(ExtraChargeForm source, ExtraChargeViewItem obj) {
            super(source, obj);
        }
    }

    public static class SaveEvent extends ExtraChargeFormEvent {
        SaveEvent(ExtraChargeForm source, ExtraChargeViewItem obj) {
            super(source, obj);
        }
    }

    public static class DeleteEvent extends ExtraChargeFormEvent {
        DeleteEvent(ExtraChargeForm source, ExtraChargeViewItem item) {
            super(source, item);
        }
    }

    public static class CloseEvent extends ExtraChargeFormEvent {
        CloseEvent(ExtraChargeForm source) {
            super(source, null);
        }
    }
}
