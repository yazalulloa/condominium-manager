package kyo.yaz.condominium.manager.core.domain.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Location {

  @JsonProperty
  private final float longitude;

  @JsonProperty
  private final float latitude;

  @JsonProperty
  private final Float horizontalAccuracy;

  @JsonProperty
  private final Integer livePeriod;

  @JsonProperty
  private final Integer heading;

  @JsonProperty
  private final Integer proximityAlertRadius;
}
