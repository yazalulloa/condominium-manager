package kyo.yaz.condominium.manager.ui.views.building;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import kyo.yaz.condominium.manager.ui.views.actions.ViewEvent;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.domain.ReserveFundViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;

import java.math.BigDecimal;

public class ReserveFundForm extends BaseForm {
    @PropertyId("name")
    private final TextField nameField = new TextField(Labels.ReserveFund.NAME_LABEL);
    @PropertyId("fund")
    private final BigDecimalField fundField = new BigDecimalField(Labels.ReserveFund.FUND_LABEL);
    @PropertyId("percentage")
    private final BigDecimalField percentageField = new BigDecimalField(Labels.ReserveFund.PERCENTAGE_LABEL);
    @PropertyId("active")
    private final Checkbox activeField = new Checkbox(Labels.ReserveFund.ACTIVE_LABEL);


    public final Binder<ReserveFundViewItem> binder = new BeanValidationBinder<>(ReserveFundViewItem.class);

    private ReserveFundViewItem item;

    public ReserveFundForm() {
        addClassName("reserve-fund-form");

        add(

                nameField,
                fundField,
                percentageField,
                activeField,
                createButtonsLayout());


        binder.bindInstanceFields(this);
        reloadItem();
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

    public void reloadItem() {
        setItem(defaultItem());
    }

    public void setItem(ReserveFundViewItem item) {
        this.item = item;
        binder.readBean(item);
    }



    public ReserveFundViewItem defaultItem() {
        return ReserveFundViewItem.builder()
                .fund(BigDecimal.ZERO)
                .percentage(BigDecimal.ZERO)
                .active(true)
                .build();
    }

    private static abstract class ReserveFundFormEvent extends ViewEvent<ReserveFundForm, ReserveFundViewItem> {

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
