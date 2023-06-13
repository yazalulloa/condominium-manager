package kyo.yaz.condominium.manager.ui.views.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.persistence.domain.ReserveFund;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReserveFundViewItem {

  @NotBlank
  @JsonProperty
  private String name;

  @NotNull
  @Digits(integer = 19, fraction = 2)
  @JsonProperty
  private BigDecimal fund;

  @Min(0)
  @NotNull
  @JsonProperty
  private BigDecimal pay;

  @Builder.Default
  @JsonProperty
  private Boolean active = true;

  @NotNull
  @JsonProperty
  private ReserveFund.Type type;

  @NotNull
  @JsonProperty
  private Expense.Type expenseType;

  @NotNull
  @Builder.Default
  @JsonProperty
  private Boolean addToExpenses = true;

}
