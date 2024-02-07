package kyo.yaz.condominium.manager.ui.views.email_config;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import io.vertx.core.json.JsonObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Consumer;
import kyo.yaz.condominium.manager.ui.views.actions.ViewEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;

public class EmailConfigForm extends BaseForm {

  @PropertyId("id")
  private final TextField idField = new TextField(Labels.EmailConfig.ID_LABEL);
  @PropertyId("from")
  private final EmailField fromField = new EmailField(Labels.EmailConfig.FROM_LABEL);
  @PropertyId("active")
  private final Checkbox activeField = new Checkbox(Labels.EmailConfig.ACTIVE_LABEL, true);
  @PropertyId("config")
  private final TextArea configField = new TextArea(Labels.EmailConfig.CONFIGURATION_LABEL);
  @PropertyId("storedCredential")
  private final TextArea storedCredentialField = new TextArea(Labels.EmailConfig.STORED_CREDENTIAL_LABEL);


  private final Button saveBtn = new Button(Labels.SAVE);
  Binder<EmailConfigViewItem> binder = new BeanValidationBinder<>(EmailConfigViewItem.class);
  EmailConfigViewItem item;

  public EmailConfigForm() {
    addClassName("email-config-form");
    init();
  }

  private void init() {

    fromField.setAutocomplete(Autocomplete.EMAIL);
    fromField.setAutocomplete(Autocomplete.ON);
    add(
        idField,
        fromField,
        activeField,
        upload("configuración", ".json", inputStream -> {
          try {
            final var json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            final var config = new JsonObject(json).encodePrettily();

            configField.setValue(config);
            //  configField.setVisible(true);
          } catch (Exception e) {
            showError(e);
          }

        }),
        configField,
        upload("credenciales", null, inputStream -> {
          try {
            final var bytes = inputStream.readAllBytes();
            final var base64 = Base64.getEncoder().encodeToString(bytes);
            storedCredentialField.setValue(base64);
          } catch (Exception e) {
            showError(e);
          }

        }),
        storedCredentialField,
        createButtonsLayout());

    // configField.setVisible(false);
    configField.setReadOnly(true);
    storedCredentialField.setReadOnly(true);
    binder.bindInstanceFields(this);
  }

  private Upload upload(String fileName, String acceptedFileTypes, Consumer<InputStream> consumer) {

    return ViewUtil.singleUpload(fileName, acceptedFileTypes, inputStream -> {
      consumer.accept(inputStream);
      uiAsyncAction(this::checkSaveBtn);

    }, event -> viewHelper.showError(event.getErrorMessage()), event -> showError(event.getReason()));
  }

  private Component createButtonsLayout() {
    final var delete = new Button(Labels.DELETE);
    final var close = new Button(Labels.CANCEL);

    saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
    close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    saveBtn.addClickShortcut(Key.ENTER);
    close.addClickShortcut(Key.ESCAPE);

    saveBtn.addClickListener(event -> validateAndSave());
    delete.addClickListener(event -> fireEvent(new DeleteEvent(this, item)));
    close.addClickListener(event -> fireEvent(new CloseEvent(this)));

    binder.addStatusChangeListener(e -> checkSaveBtn());

    return new HorizontalLayout(saveBtn, delete, close);
  }

  private void checkSaveBtn() {
    saveBtn.setEnabled(binder.isValid());
  }

  private boolean validItem() {
    return item != null && item.getConfig() != null;
  }

  private void validateAndSave() {
    try {

      final var oldItem = item;
      binder.writeBean(item);

      fireEvent(new SaveEvent(this, item));
    } catch (ValidationException e) {
      logger().error("ERROR_VALIDATING", e);

      asyncNotification(e.getMessage());

    }
  }

  public void setItem(EmailConfigViewItem item) {
    this.item = item;
    binder.readBean(item);
  }

  private static abstract class EmailConfigFormEvent extends ViewEvent<EmailConfigForm, EmailConfigViewItem> {

    protected EmailConfigFormEvent(EmailConfigForm source, EmailConfigViewItem obj) {
      super(source, obj);
    }
  }

  public static class SaveEvent extends EmailConfigFormEvent {

    SaveEvent(EmailConfigForm source, EmailConfigViewItem item) {
      super(source, item);
    }
  }

  public static class DeleteEvent extends EmailConfigFormEvent {

    DeleteEvent(EmailConfigForm source, EmailConfigViewItem item) {
      super(source, item);
    }

  }

  public static class CloseEvent extends EmailConfigFormEvent {

    CloseEvent(EmailConfigForm source) {
      super(source, null);
    }
  }
}
