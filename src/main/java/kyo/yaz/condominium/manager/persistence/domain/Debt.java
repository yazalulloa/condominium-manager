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
public class Debt {

    @JsonProperty
    private final String aptNumber;
    @JsonProperty
    private final String name;
    @JsonProperty
    private final int receipts;
    @JsonProperty
    private final BigDecimal amount;
    @JsonProperty
    private final Set<Month> months;
    @JsonProperty
    private final BigDecimal previousPaymentAmount;
    @JsonProperty
    private final Currency previousPaymentAmountCurrency;
}
