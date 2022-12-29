package kyo.yaz.condominium.manager.ui.views.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.ui.views.base.AbstractView;
import kyo.yaz.condominium.manager.ui.views.domain.BuildingViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
public class CreateBuildingForm extends FormLayout implements AbstractView {


    public final Binder<BuildingViewItem> binder = new BeanValidationBinder<>(BuildingViewItem.class);
    @PropertyId("id")
    private final TextField idTextField = new TextField("ID");
    @PropertyId("name")
    private final TextField nameTextField = new TextField("Nombre");
    @PropertyId("rif")
    private final TextField rifTextField = new TextField("RIF");
    @PropertyId("reserveFund")
    private final BigDecimalField reserveFundField = new BigDecimalField(Labels.Building.RESERVE_FUND_LABEL);
    @PropertyId("reserveFundCurrency")
    private final ComboBox<Currency> reserveFundCurrencyComboBox = ViewUtil.currencyComboBox(Labels.Building.RESERVE_FUND_CURRENCY_LABEL);
    @PropertyId("mainCurrency")
    private final ComboBox<Currency> mainCurrencyComboBox = ViewUtil.currencyComboBox(Labels.Building.MAIN_CURRENCY_LABEL);
    @PropertyId("currenciesToShowAmountToPay")
    private final MultiSelectComboBox<Currency> currenciesToShowAmountToPayComboBox = new MultiSelectComboBox<>(Labels.Building.SHOW_PAYMENT_IN_CURRENCIES, Currency.values);

    @PropertyId("fixedPay")
    private final Checkbox fixedPayField = new Checkbox(Labels.Building.FIXED_PAY_LABEL);

    @PropertyId("fixedPayAmount")
    private final BigDecimalField fixedPayAmountField = new BigDecimalField(Labels.Building.FIXED_PAY_AMOUNT_LABEL);
    @PropertyId("fixedPayCurrency")
    private final ComboBox<Currency> fixedPayCurrencyField = ViewUtil.currencyComboBox(Labels.Building.FIXED_PAY_CURRENCY_LABEL);
    BuildingViewItem building = BuildingViewItem.builder().build();

    public CreateBuildingForm() {
        addClassName("building-form");


        add(
                idTextField,
                nameTextField,
                rifTextField,
                reserveFundField,
                reserveFundCurrencyComboBox,
                mainCurrencyComboBox,
                currenciesToShowAmountToPayComboBox,
                fixedPayField,
                fixedPayAmountField,
                fixedPayCurrencyField);

        fixedPayVisibility();

        binder.bindInstanceFields(this);
        final var fixedPayAmountFieldBinding = binder.forField(fixedPayAmountField)
                .withValidator(bigDecimal -> {
                    final boolean bool = Optional.ofNullable(fixedPayField.getValue()).orElse(false);

                    if (bool) {
                        final var fixedPayAmount = Optional.ofNullable(fixedPayAmountField.getValue())
                                .orElse(BigDecimal.ZERO);
                        return DecimalUtil.greaterThanZero(fixedPayAmount);
                    }

                    return true;
                }, "Monto fijo a pagar tiene que ser mayor a cero")
                .bind(BuildingViewItem::getFixedPayAmount, BuildingViewItem::setFixedPayAmount);


        final var fixedPayCurrencyFieldBinding = binder.forField(fixedPayCurrencyField)
                .withValidator(currency -> {
                    final boolean bool = Optional.ofNullable(fixedPayField.getValue()).orElse(false);

                    if (bool) {
                        return currency != null;
                    }

                    return true;
                }, "La moneda del monto fijo a pagar tiene es requerida")
                .bind(BuildingViewItem::getFixedPayCurrency, BuildingViewItem::setFixedPayCurrency);


        fixedPayField.addValueChangeListener(e -> {
            fixedPayVisibility();
            fixedPayAmountFieldBinding.validate();
            fixedPayCurrencyFieldBinding.validate();
        });

    }

    private void fixedPayVisibility() {
        final boolean bool = Optional.ofNullable(fixedPayField.getValue())
                .orElse(false);

        fixedPayAmountField.setVisible(bool);
        //fixedPayAmountField.setRequired(bool);
        fixedPayAmountField.setRequiredIndicatorVisible(bool);
        fixedPayCurrencyField.setVisible(bool);
        fixedPayCurrencyField.setRequired(bool);
        fixedPayCurrencyField.setRequiredIndicatorVisible(bool);

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

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
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
}
