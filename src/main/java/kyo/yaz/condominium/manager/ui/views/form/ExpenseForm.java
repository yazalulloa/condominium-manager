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
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.ui.views.base.AbstractView;
import kyo.yaz.condominium.manager.ui.views.domain.ExpenseViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class ExpenseForm extends FormLayout implements AbstractView {

    @PropertyId("description")
    private final TextField descriptionField = new TextField(Labels.Expense.DESCRIPTION_LABEL);

    @PropertyId("amount")
    private final BigDecimalField amountField = new BigDecimalField(Labels.Expense.AMOUNT_LABEL);

    @PropertyId("currency")
    private final ComboBox<Currency> currencyComboBox = new ComboBox<>(Labels.Expense.CURRENCY_LABEL, Currency.values);

    @PropertyId("type")
    private final ComboBox<Expense.Type> typeComboBox = new ComboBox<>(Labels.Expense.TYPE_LABEL, Expense.Type.values);

    private final Button saveBtn = new Button(Labels.SAVE);
    private final Button deleteBtn = new Button(Labels.DELETE);
    private final Button cancelBtn = new Button(Labels.CANCEL);
    private final Binder<ExpenseViewItem> binder = new BeanValidationBinder<>(ExpenseViewItem.class);

    ExpenseViewItem expense;

    public ExpenseForm() {
        addClassName("expense-form");

        currencyComboBox.setItemLabelGenerator(Currency::name);
        typeComboBox.setItemLabelGenerator(Expense.Type::name);

        currencyComboBox.setAllowCustomValue(false);
        typeComboBox.setAllowCustomValue(false);
        currencyComboBox.setAutoOpen(true);
        typeComboBox.setAutoOpen(true);

        add(
                descriptionField,
                amountField,
                currencyComboBox,
                typeComboBox,
                createButtonsLayout());

        binder.bindInstanceFields(this);
        setExpense(ExpenseViewItem.builder().build());
    }

    private HorizontalLayout createButtonsLayout() {
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveBtn.addClickShortcut(Key.ENTER);
        cancelBtn.addClickShortcut(Key.ESCAPE);

        saveBtn.addClickListener(event -> validateAndSave());
        deleteBtn.addClickListener(event -> fireEvent(new DeleteEvent(this, expense)));
        cancelBtn.addClickListener(event -> {
            binder.readBean(null);
        });


        binder.addStatusChangeListener(e -> saveBtn.setEnabled(binder.isValid()));

        return new HorizontalLayout(saveBtn, deleteBtn, cancelBtn);
    }

    private void validateAndSave() {
        try {
            binder.writeBean(expense);
            fireEvent(new SaveEvent(this, expense));
            setExpense(ExpenseViewItem.builder().build());
        } catch (ValidationException e) {
            log.error("ERROR_VALIDATING", e);
            asyncNotification(e.getMessage());

        }
    }

    @Override
    public Component component() {
        return this;
    }

    @Override
    public Logger logger() {
        return log;
    }

    public void setExpense(ExpenseViewItem expense) {
        this.expense = expense;
        binder.readBean(expense);

    }

    public static abstract class FormEvent extends ComponentEvent<ExpenseForm> {
        private final ExpenseViewItem obj;

        protected FormEvent(ExpenseForm source, ExpenseViewItem obj) {
            super(source, false);
            this.obj = obj;
        }

        public ExpenseViewItem getObj() {
            return obj;
        }
    }

    public static class SaveEvent extends FormEvent {
        SaveEvent(ExpenseForm source, ExpenseViewItem obj) {
            super(source, obj);
        }
    }

    public static class DeleteEvent extends FormEvent {
        DeleteEvent(ExpenseForm source, ExpenseViewItem obj) {
            super(source, obj);
        }

    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
