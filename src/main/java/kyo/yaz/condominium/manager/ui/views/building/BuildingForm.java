package kyo.yaz.condominium.manager.ui.views.building;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.util.DecimalUtil;
import kyo.yaz.condominium.manager.persistence.entity.EmailConfig;
import kyo.yaz.condominium.manager.ui.views.base.BaseForm;
import kyo.yaz.condominium.manager.ui.views.domain.BuildingViewItem;
import kyo.yaz.condominium.manager.ui.views.util.Labels;
import kyo.yaz.condominium.manager.ui.views.util.ViewUtil;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class BuildingForm extends BaseForm {


    public final Binder<BuildingViewItem> binder = new BeanValidationBinder<>(BuildingViewItem.class);
    @PropertyId("id")
    private final TextField idTextField = new TextField("ID");
    @PropertyId("name")
    private final TextField nameTextField = new TextField("Nombre");
    @PropertyId("rif")
    private final TextField rifTextField = new TextField("RIF");
    @PropertyId("mainCurrency")
    private final ComboBox<Currency> mainCurrencyComboBox = ViewUtil.currencyComboBox(Labels.Building.MAIN_CURRENCY_LABEL);
    @PropertyId("debtCurrency")
    private final ComboBox<Currency> debtCurrencyComboBox = ViewUtil.currencyComboBox(Labels.Building.DEBT_CURRENCY_LABEL);
    @PropertyId("currenciesToShowAmountToPay")
    private final MultiSelectComboBox<Currency> currenciesToShowAmountToPayComboBox = new MultiSelectComboBox<>(Labels.Building.SHOW_PAYMENT_IN_CURRENCIES, Currency.values);

    @PropertyId("roundUpPayments")
    private final Checkbox roundUpPaymentsField = new Checkbox(Labels.Building.ROUND_UP_PAYMENTS_LABEL);

    @PropertyId("fixedPay")
    private final Checkbox fixedPayField = new Checkbox(Labels.Building.FIXED_PAY_LABEL);

    @PropertyId("fixedPayAmount")
    private final BigDecimalField fixedPayAmountField = new BigDecimalField(Labels.Building.FIXED_PAY_AMOUNT_LABEL);
    @PropertyId("emailConfig")
    private final ComboBox<EmailConfig> emailConfigComboBox = new ComboBox<>(Labels.Building.RECEIPT_EMAIL_FROM_LABEL);
    BuildingViewItem building = BuildingViewItem.builder().build();

    private List<EmailConfig> emailConfigs = Collections.emptyList();

    public BuildingForm() {
        addClassName("building-form");
        init();
    }

    private void init() {


        emailConfigComboBox.setItemLabelGenerator(item -> String.join(", ", item.id(), item.from()));
        emailConfigComboBox.setAutoOpen(true);
        emailConfigComboBox.setAllowCustomValue(false);

        add(
                idTextField,
                nameTextField,
                rifTextField,
                mainCurrencyComboBox,
                debtCurrencyComboBox,
                currenciesToShowAmountToPayComboBox,
                emailConfigComboBox,
                roundUpPaymentsField,
                fixedPayField,
                fixedPayAmountField);

        fixedPayVisibility();

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

        binder.forField(emailConfigComboBox)
                .asRequired()
                .bind(item -> emailConfigs.stream().filter(e -> Objects.equals(item.getEmailConfig(), e.id()))
                                .findFirst()
                                .orElse(null),
                        (item, emailConfig) -> item.setEmailConfig(emailConfig.id()));


        binder.bindInstanceFields(this);

        fixedPayField.addValueChangeListener(e -> {
            fixedPayVisibility();
            fixedPayAmountFieldBinding.validate();
        });
    }

    public void setEmailConfigs(List<EmailConfig> emailConfigs) {
        this.emailConfigs = emailConfigs;
        emailConfigComboBox.setItems(emailConfigs);
    }

    private void fixedPayVisibility() {
        final boolean bool = Optional.ofNullable(fixedPayField.getValue())
                .orElse(false);

        fixedPayAmountField.setVisible(bool);
        //fixedPayAmountField.setRequired(bool);
        fixedPayAmountField.setRequiredIndicatorVisible(bool);

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


    public static abstract class BuildingFormEvent extends ComponentEvent<BuildingForm> {
        private final BuildingViewItem building;

        protected BuildingFormEvent(BuildingForm source, BuildingViewItem building) {
            super(source, false);
            this.building = building;
        }

        public BuildingViewItem getBuilding() {
            return building;
        }
    }

    public static class SaveEvent extends BuildingFormEvent {
        SaveEvent(BuildingForm source, BuildingViewItem building) {
            super(source, building);
        }
    }
}
