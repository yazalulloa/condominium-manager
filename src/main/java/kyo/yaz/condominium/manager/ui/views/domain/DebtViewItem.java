package kyo.yaz.condominium.manager.ui.views.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kyo.yaz.condominium.manager.core.domain.Currency;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Month;
import java.util.Set;

@Jacksonized
@Builder(toBuilder = true)
@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
public class DebtViewItem {

    @NotBlank
    @JsonProperty("apt_number")
    private String aptNumber;

    @NotBlank
    @JsonProperty("name")
    private String name;

    @JsonProperty("receipts")
    private int receipts;

    @NotNull
    @JsonProperty("amount")
    private BigDecimal amount;

    @NotNull
    @JsonProperty("currency")
    private Currency currency;

    @JsonProperty
    private Set<Month> months;

    @JsonProperty("previous_payment_amount")
    private BigDecimal previousPaymentAmount;

    @JsonProperty("previous_payment_amount_currency")
    private Currency previousPaymentAmountCurrency;
}
