package kyo.yaz.condominium.manager.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kyo.yaz.condominium.manager.core.domain.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Month;
import java.util.Set;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Debt implements IAmountCurrency {

    @NotBlank
    @JsonProperty("apt_number")
    private final String aptNumber;

    @NotBlank
    @JsonProperty("name")
    private final String name;

    @JsonProperty("receipts")
    private final int receipts;

    @NotNull
    @JsonProperty("amount")
    private final BigDecimal amount;

    @NotNull
    @JsonProperty("currency")
    private final Currency currency;

    @JsonProperty
    private final Set<Month> months;

    @JsonProperty("previous_payment_amount")
    private final BigDecimal previousPaymentAmount;

    @JsonProperty("previous_payment_amount_currency")
    private final Currency previousPaymentAmountCurrency;
}
