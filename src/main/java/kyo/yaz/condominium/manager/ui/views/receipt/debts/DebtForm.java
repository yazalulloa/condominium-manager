package kyo.yaz.condominium.manager.ui.views.receipt.debts;

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
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.ui.views.actions.ViewEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.domain.DebtViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Month;

@Component
@Scope("prototype")
public class DebtForm extends BaseForm {

    @PropertyId("name")
    private final TextField nameField = new TextField(Labels.Debt.NAME_LABEL);
    @PropertyId("receipts")
    private final IntegerField receiptsField = new IntegerField(Labels.Debt.RECEIPT_LABEL);
    @PropertyId("amount")
    private final BigDecimalField amountField = new BigDecimalField(Labels.Debt.AMOUNT_LABEL);
    @PropertyId("months")
    private final MultiSelectComboBox<Month> monthsPicker = ViewUtil.monthMultiComboBox(Labels.Debt.MONTHS_LABEL);
    @PropertyId("previousPaymentAmount")
    private final BigDecimalField previousPaymentAmountField = new BigDecimalField(Labels.Debt.PREVIOUS_AMOUNT_PAYED_LABEL);
    @PropertyId("previousPaymentAmountCurrency")
    private final ComboBox<Currency> previousPaymentAmountCurrencyComboBox = ViewUtil.currencyComboBox(Labels.Debt.PREVIOUS_AMOUNT_CURRENCY_PAYED_LABEL);

    private final Binder<DebtViewItem> binder = new BeanValidationBinder<>(DebtViewItem.class);
    private final TranslationProvider translationProvider;

    DebtViewItem item;

    @Autowired
    public DebtForm(TranslationProvider translationProvider) {
        this.translationProvider = translationProvider;

        addClassName("debt-form");


        monthsPicker.setAllowCustomValue(false);
        monthsPicker.setAutoOpen(true);
        monthsPicker.setItemLabelGenerator(m -> this.translationProvider.translate(m.name()));

        TextField aptNumberField = new TextField(Labels.Debt.APT_LABEL);
        add(
                aptNumberField,
                nameField,
                receiptsField,
                amountField,
                monthsPicker,
                previousPaymentAmountField,
                previousPaymentAmountCurrencyComboBox,
                createButtonsLayout());

        binder.bindInstanceFields(this);
        setItem(DebtViewItem.builder().build());
    }

    private HorizontalLayout createButtonsLayout() {
        final var addBtn = new Button(Labels.ADD);
        final var deleteBtn = new Button(Labels.DELETE);
        final var cancelBtn = new Button(Labels.CANCEL);

        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        addBtn.addClickShortcut(Key.ENTER);
        cancelBtn.addClickShortcut(Key.ESCAPE);

        addBtn.addClickListener(event -> validateAndSave());
        deleteBtn.addClickListener(event -> fireEvent(new DeleteEvent(this, item)));
        cancelBtn.addClickListener(event -> {
            binder.readBean(null);
            fireEvent(new CloseEvent(this));
        });


        binder.addStatusChangeListener(e -> addBtn.setEnabled(binder.isValid()));

        return new HorizontalLayout(addBtn, deleteBtn, cancelBtn);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(item);
            fireEvent(new SaveEvent(this, item));
            setItem(DebtViewItem.builder().build());
        } catch (ValidationException e) {
            logger().error("ERROR_VALIDATING", e);
            asyncNotification(e.getMessage());

        }
    }

    public DebtViewItem defaultItem() {
        return DebtViewItem.builder()
                .build();
    }

    public void setItem(DebtViewItem expense) {
        this.item = expense;
        binder.readBean(expense);

    }

    public static abstract class DebtFormEvent extends ViewEvent<DebtForm, DebtViewItem> {

        DebtFormEvent(DebtForm source, DebtViewItem obj) {
            super(source, obj);
        }
    }

    public static class SaveEvent extends DebtFormEvent {
        SaveEvent(DebtForm source, DebtViewItem obj) {
            super(source, obj);
        }
    }

    public static class DeleteEvent extends DebtFormEvent {
        DeleteEvent(DebtForm source, DebtViewItem obj) {
            super(source, obj);
        }
    }

    public static class CloseEvent extends DebtFormEvent {
        CloseEvent(DebtForm source) {
            super(source, null);
        }
    }
}
