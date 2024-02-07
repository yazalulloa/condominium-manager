package kyo.yaz.condominium.manager.ui.views.building;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import java.math.BigDecimal;
import kyo.yaz.condominium.manager.core.provider.TranslationProvider;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.persistence.domain.ReserveFund;
import kyo.yaz.condominium.manager.persistence.domain.ReserveFund.Type;
import kyo.yaz.condominium.manager.ui.views.actions.ViewEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.domain.ReserveFundViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReserveFundForm extends BaseForm {

  public final Binder<ReserveFundViewItem> binder = new BeanValidationBinder<>(ReserveFundViewItem.class);
  @PropertyId("name")
  private final TextField nameField = new TextField(Labels.ReserveFund.NAME_LABEL);
  @PropertyId("fund")
  private final BigDecimalField fundField = new BigDecimalField(Labels.ReserveFund.FUND_LABEL);
  @PropertyId("pay")
  private final BigDecimalField payField = new BigDecimalField(Labels.ReserveFund.PAY_LABEL);
  @PropertyId("expense")
  private final BigDecimalField expenseField = new BigDecimalField(Labels.ReserveFund.EXPENSE_LABEL);
  @PropertyId("active")
  private final Checkbox activeField = new Checkbox(Labels.ReserveFund.ACTIVE_LABEL);
  @PropertyId("type")
  private final ComboBox<ReserveFund.Type> typeComboBox = ViewUtil.reserveFundTypeComboBox(
      Labels.ReserveFund.TYPE_LABEL);
  @PropertyId("expenseType")
  private final ComboBox<Expense.Type> expenseTypeComboBox = ViewUtil.expenseTypeComboBox(
      Labels.ReserveFund.EXPENSE_TYPE_LABEL);
  @PropertyId("addToExpenses")
  private final Checkbox addToExpensesField = new Checkbox(Labels.ReserveFund.ADD_TO_EXPENSES_LABEL);
  private final TranslationProvider translationProvider;

  private transient ReserveFundViewItem item;

  public ReserveFundForm(TranslationProvider translationProvider) {
    this.translationProvider = translationProvider;
    init();
  }

  private void init() {

    addClassName("reserve-fund-form");

    typeComboBox.setItemLabelGenerator(m -> translationProvider.translate(m.name()));
    expenseTypeComboBox.setItemLabelGenerator(m -> translationProvider.translate(m.name()));

    add(

        nameField,
        fundField,
        typeComboBox,
        payField,
        expenseField,
        activeField,
        expenseTypeComboBox,
        addToExpensesField,
        createButtonsLayout());

    binder.bindInstanceFields(this);
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

      fireEvent(new SaveEvent(this, item));
    } catch (ValidationException e) {
      logger().error("ERROR_VALIDATING", e);
      asyncNotification(e.getMessage());

    }
  }

  public void setItem(ReserveFundViewItem item) {
    this.item = item;
    binder.readBean(item);
  }


  public ReserveFundViewItem defaultItem() {
    return ReserveFundViewItem.builder()
        .fund(BigDecimal.ZERO)
        .pay(BigDecimal.ZERO)
        .active(true)
        .type(Type.PERCENTAGE)
        .expenseType(Expense.Type.COMMON)
        .addToExpenses(true)
        .build();
  }

  private abstract static class ReserveFundFormEvent extends ViewEvent<ReserveFundForm, ReserveFundViewItem> {

    protected ReserveFundFormEvent(ReserveFundForm source, ReserveFundViewItem obj) {
      super(source, obj);
    }
  }

  public static class SaveEvent extends ReserveFundFormEvent {

    SaveEvent(ReserveFundForm source, ReserveFundViewItem item) {
      super(source, item);
    }
  }

  public static class DeleteEvent extends ReserveFundFormEvent {

    DeleteEvent(ReserveFundForm source, ReserveFundViewItem item) {
      super(source, item);
    }
  }

  public static class CloseEvent extends ReserveFundFormEvent {

    CloseEvent(ReserveFundForm source) {
      super(source, null);
    }
  }
}
