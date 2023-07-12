package kyo.yaz.condominium.manager.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
@EqualsAndHashCode
public class ReserveFund {

  @JsonProperty
  private final String name;
  @JsonProperty
  private final BigDecimal fund;

  @JsonProperty
  private final BigDecimal pay;

  @Builder.Default
  @JsonProperty
  private final Boolean active = true;

  @JsonProperty
  private final Type type;

  @Builder.Default
  @JsonProperty
  private final Expense.Type expenseType = Expense.Type.COMMON;

  @Builder.Default
  @JsonProperty
  private final Boolean addToExpenses = true;

  public enum Type {
    FIXED_PAY, PERCENTAGE;


    public static final Type[] values = values();
  }
}
