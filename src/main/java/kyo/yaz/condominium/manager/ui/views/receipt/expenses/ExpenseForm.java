package kyo.yaz.condominium.manager.ui.views.receipt.expenses;

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
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.ui.views.actions.ViewEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.domain.ExpenseViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExpenseForm extends BaseForm {

    @PropertyId("description")
    private final TextField descriptionField = new TextField(Labels.Expense.DESCRIPTION_LABEL);

    @PropertyId("amount")
    private final BigDecimalField amountField = new BigDecimalField(Labels.Expense.AMOUNT_LABEL);

    @PropertyId("currency")
    private final ComboBox<Currency> currencyComboBox = ViewUtil.currencyComboBox(Labels.Expense.CURRENCY_LABEL);

    @PropertyId("type")
    private final ComboBox<Expense.Type> typeComboBox = new ComboBox<>(Labels.Expense.TYPE_LABEL, Expense.Type.values);


    private final Binder<ExpenseViewItem> binder = new BeanValidationBinder<>(ExpenseViewItem.class);
    private final TranslationProvider translationProvider;

    ExpenseViewItem item;

    @Autowired
    public ExpenseForm(TranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
        addClassName("expense-form");


        typeComboBox.setAllowCustomValue(false);
        typeComboBox.setAutoOpen(true);
        typeComboBox.setItemLabelGenerator(m -> this.translationProvider.translate(m.name()));

        add(
                descriptionField,
                amountField,
                currencyComboBox,
                typeComboBox,
                createButtonsLayout());

        binder.bindInstanceFields(this);
        setItem(ExpenseViewItem.builder().build());
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
            setItem(ExpenseViewItem.builder().build());
        } catch (ValidationException e) {
            logger().error("ERROR_VALIDATING", e);
            asyncNotification(e.getMessage());

        }
    }

    public ExpenseViewItem defaultItem() {
        return ExpenseViewItem.builder()
                .build();
    }

    public void setItem(ExpenseViewItem expense) {
        this.item = expense;
        binder.readBean(expense);

    }

    public static abstract class ExpenseFormEvent extends ViewEvent<ExpenseForm, ExpenseViewItem> {

        ExpenseFormEvent(ExpenseForm source, ExpenseViewItem obj) {
            super(source, obj);
        }
    }

    public static class SaveEvent extends ExpenseFormEvent {
        SaveEvent(ExpenseForm source, ExpenseViewItem obj) {
            super(source, obj);
        }
    }

    public static class DeleteEvent extends ExpenseFormEvent {
        DeleteEvent(ExpenseForm source, ExpenseViewItem obj) {
            super(source, obj);
        }
    }

    public static class CloseEvent extends ExpenseFormEvent {
        CloseEvent(ExpenseForm source) {
            super(source, null);
        }
    }
}
