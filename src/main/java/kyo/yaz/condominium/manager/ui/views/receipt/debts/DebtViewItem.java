package kyo.yaz.condominium.manager.ui.views.receipt.debts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Month;
import java.util.Set;
import kyo.yaz.condominium.manager.core.domain.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
@AllArgsConstructor
@EqualsAndHashCode
public class DebtViewItem {

  @NotBlank
  @JsonProperty
  private final String aptNumber;

  @NotBlank
  @JsonProperty
  private final String name;

  @Min(0)
  @JsonProperty
  private int receipts;

  @NotNull
  @Builder.Default
  @JsonProperty
  private BigDecimal amount = BigDecimal.ZERO;

  @JsonProperty
  private Set<Month> months;

  @JsonProperty
  private BigDecimal previousPaymentAmount;

  @JsonProperty
  private Currency previousPaymentAmountCurrency;
}
