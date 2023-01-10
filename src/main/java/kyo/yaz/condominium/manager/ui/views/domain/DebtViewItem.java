package kyo.yaz.condominium.manager.ui.views.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kyo.yaz.condominium.manager.core.domain.Currency;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @JsonProperty
    private String aptNumber;

    @NotBlank
    @JsonProperty
    private String name;

    @JsonProperty
    private int receipts;

    @NotNull
    @JsonProperty
    private BigDecimal amount;

    @JsonProperty
    private Set<Month> months;

    @JsonProperty
    private BigDecimal previousPaymentAmount;

    @JsonProperty
    private Currency previousPaymentAmountCurrency;
}
