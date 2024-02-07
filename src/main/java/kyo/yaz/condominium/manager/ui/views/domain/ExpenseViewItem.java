package kyo.yaz.condominium.manager.ui.views.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
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
public class ExpenseViewItem {

  @NotBlank
  @JsonProperty
  private String description;

  @NotNull
  @JsonProperty
  private BigDecimal amount;

  @NotNull
  @JsonProperty
  private Currency currency;
  @JsonProperty
  private boolean reserveFund;

  @NotNull
  @JsonProperty
  private Expense.Type type;
}
