package kyo.yaz.condominium.manager.ui.views.apartment;

import com.vaadin.flow.component.Component;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import kyo.yaz.condominium.manager.ui.views.actions.ViewEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.form.EmailsForm;
import kyo.yaz.condominium.manager.ui.views.util.Labels;

public class ApartmentForm extends BaseForm {

  @PropertyId("building_id")
  private final ComboBox<String> buildingField = new ComboBox<>(Labels.Apartment.BUILDING_LABEL);
  @PropertyId("number")
  private final TextField numberField = new TextField(Labels.Apartment.NUMBER_LABEL);

  @PropertyId("name")
  private final TextField nameField = new TextField(Labels.Apartment.NAME_LABEL);

  @PropertyId("idDoc")
  private final TextField idDocField = new TextField(Labels.Apartment.ID_DOC_LABEL);

    /*@PropertyId("paymentType")
    private final ComboBox<PaymentType> paymentTypeComboBox = new ComboBox<>(Labels.Apartment.PAYMENT_TYPE_LABEL, PaymentType.values);*/

  @PropertyId("amountToPay")
  private final BigDecimalField amountToPayField = new BigDecimalField(Labels.Apartment.ALIQUOT_LABEL);


  private final EmailsForm emailsForm = new EmailsForm();


  ApartmentViewItem item;

  Binder<ApartmentViewItem> binder = new BeanValidationBinder<>(ApartmentViewItem.class);

  public ApartmentForm() {
    addClassName("apartment-form");
    init();
  }

  private void init() {
    add(
        buildingField,
        numberField,
        nameField,
        idDocField,
        // paymentTypeComboBox,
        amountToPayField,
        emailsForm,
        createButtonsLayout());

    binder.bindInstanceFields(this);
  }


  public void setBuildingIds(Collection<String> buildingIds) {
    buildingField.setItems(buildingIds);
  }

  private Component createButtonsLayout() {

    final var save = new Button(Labels.SAVE);
    final var delete = new Button(Labels.DELETE);
    final var close = new Button(Labels.CANCEL);

    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
    close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    save.addClickShortcut(Key.ENTER);
    close.addClickShortcut(Key.ESCAPE);

    save.addClickListener(event -> validateAndSave());
    delete.addClickListener(event -> fireEvent(new DeleteEvent(this, item)));
    close.addClickListener(event -> fireEvent(new CloseEvent(this)));

    binder.addStatusChangeListener(e -> save.setEnabled(binder.isValid()));

    return new HorizontalLayout(save, delete, close);
  }

  private void validateAndSave() {
    try {
      item.setEmails(emailsForm.getEmails());
      binder.writeBean(item);

      fireEvent(new SaveEvent(this, item));
    } catch (ValidationException e) {
      logger().error("ERROR_VALIDATING", e);

      asyncNotification(e.getMessage());

    }
  }

  public void setItem(ApartmentViewItem item) {
    this.item = item;
    binder.readBean(item);

    final var emails = Optional.ofNullable(item)
        .map(ApartmentViewItem::getEmails)
        .orElseGet(Collections::emptySet);

    emailsForm.clearEmailComponents();
    emailsForm.setEmails(emails);
  }

  private static abstract class ApartmentFormEvent extends ViewEvent<ApartmentForm, ApartmentViewItem> {

    protected ApartmentFormEvent(ApartmentForm source, ApartmentViewItem obj) {
      super(source, obj);
    }
  }

  public static class SaveEvent extends ApartmentFormEvent {

    SaveEvent(ApartmentForm source, ApartmentViewItem Apartment) {
      super(source, Apartment);
    }
  }

  public static class DeleteEvent extends ApartmentFormEvent {

    DeleteEvent(ApartmentForm source, ApartmentViewItem Apartment) {
      super(source, Apartment);
    }

  }

  public static class CloseEvent extends ApartmentFormEvent {

    CloseEvent(ApartmentForm source) {
      super(source, null);
    }
  }
}
