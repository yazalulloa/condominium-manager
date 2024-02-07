package kyo.yaz.condominium.manager.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import kyo.yaz.condominium.manager.core.domain.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
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
public class ExtraCharge {

  @JsonProperty
  private final String aptNumber;
  @JsonProperty
  private final String name;
  @JsonProperty
  private final String description;
  @JsonProperty
  private final BigDecimal amount;
  @JsonProperty
  private final Currency currency;
}
