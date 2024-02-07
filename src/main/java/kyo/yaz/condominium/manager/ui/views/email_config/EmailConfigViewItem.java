package kyo.yaz.condominium.manager.ui.views.email_config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
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
public class EmailConfigViewItem {

  @NotBlank
  @JsonProperty
  private String id;

  @Email
  @NotBlank
  @JsonProperty
  private String from;

  @NotBlank
  @NotNull
  @JsonProperty
  private String config;

  @NotBlank
  @NotNull
  @JsonProperty
  private String storedCredential;

  @NotNull
  @JsonProperty
  private Boolean active;

  @JsonProperty
  private Boolean isAvailable;

  @JsonProperty
  private String error;

  @JsonProperty
  private ZonedDateTime createdAt;

  @JsonProperty
  private ZonedDateTime updatedAt;

  @JsonProperty
  private ZonedDateTime lastCheckAt;
}
