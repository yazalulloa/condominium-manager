package kyo.yaz.condominium.manager.ui.views.extracharges;

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
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.ui.views.actions.ViewEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class ExtraChargeForm extends BaseForm {

    private final ComboBox<Apartment> aptComboBox = new ComboBox<>(Labels.ExtraCharge.APT_LABEL);

    @PropertyId("description")
    private final TextField descriptionField = new TextField(Labels.ExtraCharge.DESCRIPTION_LABEL);

    @PropertyId("amount")
    private final BigDecimalField amountField = new BigDecimalField(Labels.ExtraCharge.AMOUNT_LABEL);

    @PropertyId("currency")
    private final ComboBox<Currency> currencyComboBox = ViewUtil.currencyComboBox(Labels.ExtraCharge.CURRENCY_LABEL);


    private List<Apartment> apartments = Collections.emptyList();
    private final Binder<ExtraChargeViewItem> binder = new BeanValidationBinder<>(ExtraChargeViewItem.class);

    ExtraChargeViewItem item;


    public ExtraChargeForm() {
        super();
        addClassName("extra-charge-form");


        aptComboBox.setAllowCustomValue(false);
        aptComboBox.setItemLabelGenerator(apt -> "%s %s".formatted(apt.apartmentId().number(), apt.name()));


        binder.forField(aptComboBox)
                .asRequired()
                .bind(item -> apartments.stream().filter(e -> Objects.equals(item.getAptNumber(), e.apartmentId().number()))
                                .findFirst()
                                .orElse(null),
                        (item, apartment) -> {
                            item.setAptNumber(apartment.apartmentId().number());
                            item.setName(apartment.name());
                        });

        add(
                aptComboBox,
                descriptionField,
                amountField,
                currencyComboBox,
                createButtonsLayout());

        binder.bindInstanceFields(this);
    }

    public void setApartments(List<Apartment> apartments) {
        this.apartments = apartments;
        aptComboBox.setItems(apartments);
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
        } catch (ValidationException e) {
            logger().error("ERROR_VALIDATING", e);
            asyncNotification(e.getMessage());

        }
    }

    public void setItem(ExtraChargeViewItem viewItem) {
        this.item = viewItem;
        binder.readBean(viewItem);

    }


    private static abstract class ExtraChargeFormEvent extends ViewEvent<ExtraChargeForm, ExtraChargeViewItem> {

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
