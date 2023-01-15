package kyo.yaz.condominium.manager.ui.views.form;

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
import kyo.yaz.condominium.manager.core.domain.PaymentType;
import kyo.yaz.condominium.manager.ui.views.actions.FormEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.domain.ApartmentViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class CreateApartmentForm extends BaseForm {

    @PropertyId("building_id")
    private final ComboBox<String> buildingField = new ComboBox<>(Labels.Apartment.BUILDING_LABEL);
    @PropertyId("number")
    private final TextField numberField = new TextField(Labels.Apartment.NUMBER_LABEL);

    @PropertyId("name")
    private final TextField nameField = new TextField(Labels.Apartment.NAME_LABEL);

    @PropertyId("idDoc")
    private final TextField idDocField = new TextField(Labels.Apartment.ID_DOC_LABEL);

    @PropertyId("paymentType")
    private final ComboBox<PaymentType> paymentTypeComboBox = new ComboBox<>(Labels.Apartment.PAYMENT_TYPE_LABEL, PaymentType.values);

    @PropertyId("amountToPay")
    private final BigDecimalField amountToPayField = new BigDecimalField(Labels.Apartment.AMOUNT_LABEL);


    private final EmailsForm emailsForm = new EmailsForm();



    ApartmentViewItem apartment;

    Binder<ApartmentViewItem> binder = new BeanValidationBinder<>(ApartmentViewItem.class);

    public CreateApartmentForm() {
        addClassName("apartment-form");

        paymentTypeComboBox.setItemLabelGenerator(PaymentType::name);

        // configureGrid();

        add(
                buildingField,
                numberField,
                nameField,
                idDocField,
                paymentTypeComboBox,
                amountToPayField,
                emailsForm,
                createButtonsLayout());


        binder.bindInstanceFields(this);


      /*  binder.bind(buildingField, ApartmentViewItem::getBuildingId, ApartmentViewItem::setBuildingId);
        binder.bind(numberField, ApartmentViewItem::getNumber, ApartmentViewItem::setNumber);
        binder.bind(nameField, ApartmentViewItem::getName, ApartmentViewItem::setName);
        binder.bind(buildingField, ApartmentViewItem::getBuildingId, ApartmentViewItem::setBuildingId);
        binder.bind(buildingField, ApartmentViewItem::getBuildingId, ApartmentViewItem::setBuildingId);
        binder.bind(buildingField, ApartmentViewItem::getBuildingId, ApartmentViewItem::setBuildingId);*/
    }


    public void setBuildingIds(Set<String> buildingIds) {
        buildingField.setItems(buildingIds);
    }

    private HorizontalLayout createButtonsLayout() {

        final var save = new Button(Labels.SAVE);
        final var delete = new Button(Labels.DELETE);
        final var close = new Button(Labels.CANCEL);

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, apartment)));
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));


        binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

        return new HorizontalLayout(save, delete, close);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(apartment);

            fireEvent(new SaveEvent(this, apartment));
        } catch (ValidationException e) {
            logger().error("ERROR_VALIDATING", e);

            asyncNotification(e.getMessage());

        }
    }

    public void setApartment(ApartmentViewItem apartment) {
        this.apartment = apartment;
        binder.readBean(apartment);

        final var emails = Optional.ofNullable(apartment)
                .map(ApartmentViewItem::getEmails)
                .orElseGet(Collections::emptySet);

        emailsForm.clearEmailComponents();
        emailsForm.setEmails(emails);
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    private static abstract class ApartmentFormEvent extends FormEvent<CreateApartmentForm, ApartmentViewItem> {

        protected ApartmentFormEvent(CreateApartmentForm source, ApartmentViewItem obj) {
            super(source, obj);
        }
    }

    public static class SaveEvent extends ApartmentFormEvent {
        SaveEvent(CreateApartmentForm source, ApartmentViewItem Apartment) {
            super(source, Apartment);
        }
    }

    public static class DeleteEvent extends ApartmentFormEvent {
        DeleteEvent(CreateApartmentForm source, ApartmentViewItem Apartment) {
            super(source, Apartment);
        }

    }

    public static class CloseEvent extends ApartmentFormEvent {
        CloseEvent(CreateApartmentForm source) {
            super(source, null);
        }
    }
}
