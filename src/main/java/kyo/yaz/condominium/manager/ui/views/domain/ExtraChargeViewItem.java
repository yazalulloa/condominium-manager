package kyo.yaz.condominium.manager.ui.views.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kyo.yaz.condominium.manager.core.domain.Currency;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

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
public class ExtraChargeViewItem {

    @NotBlank
    @JsonProperty
    private String aptNumber;
    @NotBlank
    @JsonProperty
    private String description;
    @NotNull
    @JsonProperty
    private BigDecimal amount;
    @NotNull
    @JsonProperty
    private Currency currency;
}
