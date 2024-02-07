package kyo.yaz.condominium.manager.ui.views.receipt;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.EffectAllowed;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.core.service.entity.ApartmentService;
import kyo.yaz.condominium.manager.core.service.entity.BuildingService;
import kyo.yaz.condominium.manager.core.util.StringUtil;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.ui.views.actions.ViewEvent;
import kyo.yaz.condominium.manager.ui.views.component.DragDropDiv;
import kyo.yaz.condominium.manager.ui.views.component.DragDropList;
import kyo.yaz.condominium.manager.ui.views.component.ProgressLayout;
import kyo.yaz.condominium.manager.ui.views.domain.EmailAptReceiptRequest;
import kyo.yaz.condominium.manager.ui.views.util.AppUtil;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class EmailAptReceiptDialog extends ConfirmDialog {

  private final ProgressLayout progressLayout = new ProgressLayout();
  private final TextField subjectField = new TextField("Sujeto");
  private final TextArea msgField = new TextArea("Mensaje");
  private final Button confirmButton = new Button("Enviar recibos");
  private final Checkbox selectAllCheckbox = new Checkbox("Seleccionar todos", true);
  private final DragDropList<Apartment, Apt> apartmentsDivs = new DragDropList<>();
  private final ApartmentService apartmentService;
  private final BuildingService buildingService;
  private final TranslationProvider translationProvider;
  private Receipt receipt;
  private Building building;

  public EmailAptReceiptDialog(ApartmentService apartmentService, BuildingService buildingService,
      TranslationProvider translationProvider) {
    this.apartmentService = apartmentService;
    this.buildingService = buildingService;
    this.translationProvider = translationProvider;
    init();
  }

  private void init() {
    setWidth(40, Unit.EM);
    subjectField.setValue(AppUtil.DFLT_EMAIL_SUBJECT);
    msgField.setValue(AppUtil.DFLT_EMAIL_SUBJECT);
    subjectField.setWidthFull();
    msgField.setWidthFull();

    setCancelable(true);
    addCancelListener(e -> close());
    setCancelText(Labels.CANCEL);

    confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    setConfirmButton(confirmButton);
    addConfirmListener(e -> {
      final var apartments = apartmentsDivs.components().stream()
          .filter(Apt::isSelected)
          .map(DragDropDiv::item)
          .toList();

      fireEvent(new SendEmailsEvent(this, new EmailAptReceiptRequest(
          StringUtil.trim(subjectField.getValue()),
          StringUtil.trim(msgField.getValue()),
          receipt, building, apartments)));
      close();
    });

    subjectField.addValueChangeListener(e -> isReady());

    msgField.addValueChangeListener(e -> isReady());

    selectAllCheckbox.addValueChangeListener(e -> {
      log.info("Select all: {}", e.getValue());
      apartmentsDivs.components().forEach(apt -> apt.checkbox.setValue(e.getValue()));
      isReady();
    });

    add(
        progressLayout,
        subjectField,
        msgField,
        selectAllCheckbox,
        apartmentsDivs
    );
  }

  private void isReady() {

    final var isOneSelected = apartmentsDivs.components().stream()
        .map(Apt::isSelected)
        .filter(b -> b)
        .findFirst()
        .orElse(false);

    final var isReady = StringUtil.isNotEmpty(subjectField.getValue())
        && StringUtil.isNotEmpty(msgField.getValue())
        && isOneSelected;

    if (confirmButton.isEnabled() != isReady) {
      confirmButton.setEnabled(isReady);
    }
  }

  public void setReceipt(Receipt receipt) {
    this.receipt = receipt;

    final var month = translationProvider.translate(receipt.month().name());
    setHeader(receipt.id() + " " + receipt.buildingId() + " " + month + " " + receipt.date());
  }


  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    confirmButton.setEnabled(false);
    hide(false);

    progressLayout.progressBar().setIndeterminate(true);
    progressLayout.setProgressText("Buscando data");
    initData();
  }

  private void hide(boolean hide) {
    progressLayout.setVisible(!hide);
    subjectField.setVisible(hide);
    msgField.setVisible(hide);
    selectAllCheckbox.setVisible(hide);
    apartmentsDivs.setVisible(hide);
  }

  private void initData() {
    apartmentsDivs.components().clear();
    apartmentsDivs.removeAll();
    selectAllCheckbox.setValue(true);

    final var apartmentsByBuilding = apartmentService.rxApartmentsByBuilding(receipt.buildingId())
        .subscribeOn(Schedulers.io());

    final var buildingSingle = buildingService.get(receipt.buildingId())
        .subscribeOn(Schedulers.io());

    Single.zip(buildingSingle, apartmentsByBuilding, (building, apartments) -> {
          this.building = building;

          return (Runnable) () -> {

            apartments.forEach(apartment -> {
              final var apt = new Apt(apartment, this::isReady);
              apt.addClassName("card");
              apartmentsDivs.addComponent(apt);
            });
            isReady();
            hide(true);
          };
        })
        .subscribeOn(Schedulers.io())
        .subscribe(r -> {
          getUI().ifPresent(ui -> ui.access(r::run));
        }, t -> {
          t.printStackTrace();
          close();
        });
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
      ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }

  private static class Apt extends DragDropDiv<Apartment> {

    private final Checkbox checkbox = new Checkbox(true);

    public Apt(Apartment item, Runnable checkboxValueListener) {
      super(item);
      setDraggable(false);
      setDropEffect(DropEffect.NONE);
      setEffectAllowed(EffectAllowed.NONE);
      checkbox.addValueChangeListener(e -> checkboxValueListener.run());

      add(
          new Span(item.apartmentId().number()),
          new Span(item.name()),
          checkbox
      );

    }

    public boolean isSelected() {
      return checkbox.getValue();
    }
  }

  private static abstract class EmailAptReceiptDialogEvent extends
      ViewEvent<EmailAptReceiptDialog, EmailAptReceiptRequest> {

    protected EmailAptReceiptDialogEvent(EmailAptReceiptDialog source, EmailAptReceiptRequest obj) {
      super(source, obj);
    }
  }

  public static class SendEmailsEvent extends EmailAptReceiptDialogEvent {

    SendEmailsEvent(EmailAptReceiptDialog source, EmailAptReceiptRequest Apartment) {
      super(source, Apartment);
    }
  }
}
