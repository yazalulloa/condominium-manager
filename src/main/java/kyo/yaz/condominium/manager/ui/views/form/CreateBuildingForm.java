package kyo.yaz.condominium.manager.ui.views.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.ui.views.base.AbstractView;
import kyo.yaz.condominium.manager.ui.views.domain.BuildingViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class CreateBuildingForm extends FormLayout implements AbstractView {


    @PropertyId("id")
    private final TextField idTextField = new TextField("ID");
    @PropertyId("name")
    private final TextField nameTextField = new TextField("Nombre");
    @PropertyId("rif")
    private final TextField rifTextField = new TextField("RIF");
    @PropertyId("reserveFund")
    private final BigDecimalField reserveFundField = new BigDecimalField(Labels.Building.RESERVE_FUND_LABEL);
    @PropertyId("reserveFundCurrency")
    private final ComboBox<Currency> reserveFundCurrencyComboBox = new ComboBox<>(Labels.Building.RESERVE_FUND_CURRENCY_LABEL, Currency.values);
    @PropertyId("mainCurrency")
    private final ComboBox<Currency> mainCurrencyComboBox = new ComboBox<>(Labels.Building.MAIN_CURRENCY_LABEL, Currency.values);
    @PropertyId("currenciesToShowAmountToPay")
    private final MultiSelectComboBox<Currency> currenciesToShowAmountToPayComboBox = new MultiSelectComboBox<>(Labels.Building.SHOW_PAYMENT_IN_CURRENCIES, Currency.values);


   /* private final Button save = new Button(Labels.SAVE);
    private final Button delete = new Button(Labels.DELETE);
    private final Button close = new Button(Labels.CANCEL);*/

    BuildingViewItem building = BuildingViewItem.builder().build();

    public final Binder<BuildingViewItem> binder = new BeanValidationBinder<>(BuildingViewItem.class);

    public CreateBuildingForm() {
        addClassName("building-form");

        mainCurrencyComboBox.setItemLabelGenerator(Currency::name);
        currenciesToShowAmountToPayComboBox.setItemLabelGenerator(Currency::name);

        add(
                idTextField,
                nameTextField,
                rifTextField,
                reserveFundField,
                reserveFundCurrencyComboBox,
                mainCurrencyComboBox,
                currenciesToShowAmountToPayComboBox);

        binder.bindInstanceFields(this);
    }


    public void validateAndSave() {
        try {
            binder.writeBean(building);

            fireEvent(new SaveEvent(this, building));
        } catch (ValidationException e) {
            logger().error("ERROR_VALIDATING", e);
            asyncNotification(e.getMessage());

        }
    }

    public void setBuilding(BuildingViewItem building) {
        this.building = building;
        binder.readBean(building);
    }

    @Override
    public Component component() {
        return this;
    }

    @Override
    public Logger logger() {
        return log;
    }

    public static abstract class BuildingFormEvent extends ComponentEvent<CreateBuildingForm> {
        private final BuildingViewItem building;

        protected BuildingFormEvent(CreateBuildingForm source, BuildingViewItem building) {
            super(source, false);
            this.building = building;
        }

        public BuildingViewItem getBuilding() {
            return building;
        }
    }

    public static class SaveEvent extends BuildingFormEvent {
        SaveEvent(CreateBuildingForm source, BuildingViewItem building) {
            super(source, building);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
