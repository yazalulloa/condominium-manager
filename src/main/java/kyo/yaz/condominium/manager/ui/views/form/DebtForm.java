package kyo.yaz.condominium.manager.ui.views.form;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.domain.DebtViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;

import java.time.Month;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;


public class DebtForm extends BaseForm {

    @PropertyId("aptNumber")
    private final ComboBox<String> aptNumberComboBox = new ComboBox<>(Labels.Debt.APT_NUMBER_LABEL);
    @PropertyId("name")
    private final TextField nameField = new TextField(Labels.Debt.NAME_LABEL);
    @PropertyId("receipts")
    private final IntegerField receiptsField = new IntegerField(Labels.Debt.RECEIPT_LABEL);
    @PropertyId("amount")
    private final BigDecimalField amountField = new BigDecimalField(Labels.Debt.AMOUNT_LABEL);
    @PropertyId("months")
    private final MultiSelectComboBox<Month> monthComboBox = new MultiSelectComboBox<>(Labels.Debt.MONTHS_LABEL, Month.values());
    @PropertyId("previousPaymentAmount")
    private final BigDecimalField previousPaymentAmountField = new BigDecimalField(Labels.Debt.PREVIOUS_AMOUNT_PAYED_LABEL);
    @PropertyId("previousPaymentAmountCurrency")
    private final ComboBox<Currency> previousPaymentAmountCurrencyComboBox = ViewUtil.currencyComboBox(Labels.Debt.PREVIOUS_AMOUNT_CURRENCY_PAYED_LABEL);


    private final Button addBtn = new Button(Labels.ADD);
    private final Button deleteBtn = new Button(Labels.DELETE);
    private final Button cancelBtn = new Button(Labels.CANCEL);
    private final Binder<DebtViewItem> binder = new BeanValidationBinder<>(DebtViewItem.class);

    DebtViewItem debt;

    public DebtForm(Function<String, Single<String>> nameFunction) {
        addClassName("debt-form");


        monthComboBox.setItemLabelGenerator(Month::name);
        aptNumberComboBox.setAllowCustomValue(false);
        monthComboBox.setAllowCustomValue(false);

        add(
                aptNumberComboBox,
                nameField,
                receiptsField,
                amountField,
                monthComboBox,
                previousPaymentAmountField,
                previousPaymentAmountCurrencyComboBox,
                createButtonsLayout());

        nameField.setReadOnly(true);

        binder.bindInstanceFields(this);
        setDebt(DebtViewItem.builder().build());

        aptNumberComboBox.addValueChangeListener(event -> {

            final var aptNumber = event.getValue();

            nameFunction.apply(aptNumber)
                    .map(name -> (Runnable) () -> nameField.setValue(name))
                    .doOnSuccess(this::uiAsyncAction)
                    .ignoreElement()
                    .subscribeOn(Schedulers.io())
                    .subscribe(completableObserver());

        });
    }

    public void clearAptNumbers() {
        setAptNumbers(Collections.emptyList());
    }

    public void setAptNumbers(Collection<String> collection) {

        aptNumberComboBox.setItems(collection);
    }

    private HorizontalLayout createButtonsLayout() {
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        addBtn.addClickShortcut(Key.ENTER);
        cancelBtn.addClickShortcut(Key.ESCAPE);

        addBtn.addClickListener(event -> validateAndSave());
        deleteBtn.addClickListener(event -> fireEvent(new DebtForm.DeleteEvent(this, debt)));
        cancelBtn.addClickListener(event -> {
            binder.readBean(null);
        });


        binder.addStatusChangeListener(e -> addBtn.setEnabled(binder.isValid()));

        return new HorizontalLayout(addBtn, deleteBtn, cancelBtn);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(debt);
            fireEvent(new DebtForm.SaveEvent(this, debt));
            setDebt(DebtViewItem.builder().build());
        } catch (ValidationException e) {
            logger().error("ERROR_VALIDATING", e);
            asyncNotification(e.getMessage());

        }
    }

    public void setDebt(DebtViewItem debt) {
        this.debt = debt;
        binder.readBean(debt);
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

    public static abstract class FormEvent extends ComponentEvent<DebtForm> {
        private final DebtViewItem obj;

        protected FormEvent(DebtForm source, DebtViewItem obj) {
            super(source, false);
            this.obj = obj;
        }

        public DebtViewItem getObj() {
            return obj;
        }
    }

    public static class SaveEvent extends DebtForm.FormEvent {
        SaveEvent(DebtForm source, DebtViewItem obj) {
            super(source, obj);
        }
    }

    public static class DeleteEvent extends DebtForm.FormEvent {
        DeleteEvent(DebtForm source, DebtViewItem obj) {
            super(source, obj);
        }

    }
}
