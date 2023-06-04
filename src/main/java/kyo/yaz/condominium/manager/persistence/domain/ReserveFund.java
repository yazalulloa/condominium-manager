package kyo.yaz.condominium.manager.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
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
public class ReserveFund {

    @JsonProperty
    private final String name;
    @JsonProperty
    private final BigDecimal fund;

    @JsonProperty
    private final BigDecimal pay;

    @Builder.Default
    @JsonProperty
    private final boolean active = true;

    @JsonProperty
    private final Type type;

    public enum Type {
        FIXED_PAY, PERCENTAGE;


        public static final Type[] values = values();
    }
}
