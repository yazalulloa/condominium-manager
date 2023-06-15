package kyo.yaz.condominium.manager.ui.views.telegram_chat;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.persistence.domain.NotificationEvent;
import kyo.yaz.condominium.manager.persistence.entity.TelegramChat;
import kyo.yaz.condominium.manager.ui.views.actions.ViewEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TelegramChatForm extends BaseForm {


  @PropertyId("notificationEvents")
  private final MultiSelectComboBox<NotificationEvent> notificationEventsComboBox = ViewUtil.enumMultiComboBox(
      Labels.TelegramChat.NOTIFICATION_LABEL, NotificationEvent.values);

  public final Binder<TelegramChat> binder = new BeanValidationBinder<>(TelegramChat.class);
  TelegramChat item = TelegramChat.builder().build();

  private final TranslationProvider translationProvider;

  public TelegramChatForm(TranslationProvider translationProvider) {
    this.translationProvider = translationProvider;
    addClassName("telegram-chat-form");
    init();
  }

  private void init() {
    notificationEventsComboBox.setItemLabelGenerator(e -> translationProvider.translate(e.name()));

    binder.forField(notificationEventsComboBox)
            .bind(TelegramChat::notificationEvents, TelegramChat::notificationEvents);

    add(
        notificationEventsComboBox,
        createButtonsLayout()
    );

    binder.bindInstanceFields(this);
  }

  private com.vaadin.flow.component.Component createButtonsLayout() {

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
      binder.writeBean(item);
      fireEvent(new SaveEvent(this, item));
    } catch (ValidationException e) {
      logger().error("ERROR_VALIDATING", e);

      asyncNotification(e.getMessage());

    }
  }

  public void setItem(TelegramChat item) {
    this.item = item;
    binder.readBean(item);
  }

  private static abstract class TelegramChatFormEvent extends ViewEvent<TelegramChatForm, TelegramChat> {

    protected TelegramChatFormEvent(TelegramChatForm source, TelegramChat obj) {
      super(source, obj);
    }
  }

  public static class SaveEvent extends TelegramChatFormEvent {

    SaveEvent(TelegramChatForm source, TelegramChat TelegramChat) {
      super(source, TelegramChat);
    }
  }

  public static class DeleteEvent extends TelegramChatFormEvent {

    DeleteEvent(TelegramChatForm source, TelegramChat TelegramChat) {
      super(source, TelegramChat);
    }

  }

  public static class CloseEvent extends TelegramChatFormEvent {

    CloseEvent(TelegramChatForm source) {
      super(source, null);
    }
  }
}
