package kyo.yaz.condominium.manager.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import kyo.yaz.condominium.manager.core.domain.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
@Document("rates")
public class Rate {

  @Id
  @JsonProperty
  private final Long id;

  @JsonProperty
  private final Currency fromCurrency;

  @JsonProperty
  private final Currency toCurrency;

  @JsonProperty
  @Field(targetType = FieldType.DECIMAL128)
  private final BigDecimal rate;

    /*@JsonProperty
    private final  BigDecimal roundedRate;*/

  @JsonProperty
  private final LocalDate dateOfRate;

  @JsonProperty
  private final Source source;

  @JsonProperty
  private final ZonedDateTime createdAt;

  @JsonProperty
  private final String description;


  public enum Source {
    BCV, PLATFORM;

    public static final Source[] values = values();
  }
}
