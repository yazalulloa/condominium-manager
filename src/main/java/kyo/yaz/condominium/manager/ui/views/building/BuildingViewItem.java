package kyo.yaz.condominium.manager.ui.views.building;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;
import kyo.yaz.condominium.manager.core.domain.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
public class BuildingViewItem {
  @NotBlank
  @NotNull
  @JsonProperty
  private String id;
  @NotBlank
  @NotNull
  @JsonProperty
  private String name;
  @NotBlank
  @NotNull
  @JsonProperty
  private String rif;
  @NotNull
  @JsonProperty
  private Currency mainCurrency;
  @NotNull
  @JsonProperty
  private Currency debtCurrency;
  @NotEmpty
  @JsonProperty
  private Set<Currency> currenciesToShowAmountToPay;
  @JsonProperty
  private boolean fixedPay;
  @JsonProperty
  private BigDecimal fixedPayAmount;
  @NotNull
  @JsonProperty
  private Boolean roundUpPayments;
  @NotNull
  @NotBlank
  @JsonProperty
  private String emailConfig;
  @JsonProperty
  private final ZonedDateTime createdAt;
  @JsonProperty
  private final ZonedDateTime updatedAt;
}
