package kyo.yaz.condominium.manager.ui.views.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.persistence.domain.ExtraCharge;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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
    @JsonProperty
    private String id;

    @NotBlank
    @JsonProperty
    private String name;

    @NotBlank
    @JsonProperty
    private String rif;
    @NotNull
    @JsonProperty
    private BigDecimal reserveFund;
    @NotNull
    @JsonProperty
    private Currency reserveFundCurrency;

    @NotNull
    @JsonProperty
    private Currency mainCurrency;

    @NotEmpty
    @JsonProperty
    private Set<Currency> currenciesToShowAmountToPay;

    @JsonProperty
    private List<ExtraCharge> extraCharges;
}
