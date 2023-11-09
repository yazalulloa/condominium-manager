package kyo.yaz.condominium.manager.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReserveFund {

  @JsonProperty
  private final String name;
  @JsonProperty
  private final BigDecimal fund;
  @JsonProperty
  private final BigDecimal expense;
  @JsonProperty
  private final BigDecimal pay;
  @JsonProperty
  private final Boolean active;
  @JsonProperty
  private final Type type;
  @Builder.Default
  @JsonProperty
  private final Expense.Type expenseType = Expense.Type.COMMON;
  @JsonProperty
  private final Boolean addToExpenses;

  public enum Type {
    FIXED_PAY, PERCENTAGE;


    public static final Type[] VALUES = values();
  }
}
