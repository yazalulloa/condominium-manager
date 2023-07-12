package kyo.yaz.condominium.manager.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kyo.yaz.condominium.manager.core.domain.Currency;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
@EqualsAndHashCode
public class Expense implements IAmountCurrency {

    @NotBlank
    @JsonProperty
    private final String description;

    @NotNull
    @JsonProperty
    private final BigDecimal amount;

    @NotNull
    @JsonProperty
    private final Currency currency;

    @NotNull
    @JsonProperty
    private final Boolean reserveFund;

    @NotNull
    @JsonProperty
    private final Type type;

    public enum Type {
        COMMON, UNCOMMON;

        public static final Type[] values = values();
    }
}
