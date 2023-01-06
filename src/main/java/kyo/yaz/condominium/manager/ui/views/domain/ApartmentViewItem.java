package kyo.yaz.condominium.manager.ui.views.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kyo.yaz.condominium.manager.core.domain.PaymentType;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.Set;

@Jacksonized
@Builder(toBuilder = true)
@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
public class ApartmentViewItem {

    @JsonProperty
    private String buildingId;

    @JsonProperty
    private String number;

    @JsonProperty
    private String name;

    @JsonProperty
    private String idDoc;

    @JsonProperty
    private Set<String> emails;

    @JsonProperty
    private PaymentType paymentType;

    @JsonProperty
    private BigDecimal amountToPay;
}
