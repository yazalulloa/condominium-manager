package kyo.yaz.condominium.manager.ui.views.receipt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.Month;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
public class ReceiptFormItem {

  @NotBlank
  @JsonProperty
  private String buildingId;

  @NotNull
  @JsonProperty
  private Integer year;

  @NotNull
  @JsonProperty
  private Month month;

  @NotNull
  @JsonProperty
  private Rate rate;

  @NotNull
  @JsonProperty
  private LocalDate date;
}
