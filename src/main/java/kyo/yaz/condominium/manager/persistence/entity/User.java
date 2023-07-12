package kyo.yaz.condominium.manager.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.ZonedDateTime;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Document("users")
@EqualsAndHashCode
public class User {

  @Id
  @JsonProperty
  private final String id;

  @JsonProperty
  private final String givenName;

  @JsonProperty
  private final String name;

  @JsonProperty
  private final String email;

  @JsonProperty
  private final String picture;

  @JsonProperty
  private final String nonce;

  @JsonProperty
  private final String authorizedParty;

  @JsonProperty
  private final String userInfoStr;

  @JsonProperty
  private final String claimsStr;

  @JsonProperty
  private final String authoritiesStr;

  @JsonProperty
  private final String lastAccessTokenHash;

  @JsonProperty
  private final ZonedDateTime lastAccessTokenHashDate;

  @JsonProperty
  private final ZonedDateTime issuedAt;
  @JsonProperty
  private final ZonedDateTime expirationAt;

  @JsonProperty
  private final ZonedDateTime createdAt;
}
