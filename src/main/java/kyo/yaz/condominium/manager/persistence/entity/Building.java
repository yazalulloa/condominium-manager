package kyo.yaz.condominium.manager.persistence.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.domain.ReceiptEmailFrom;
import kyo.yaz.condominium.manager.persistence.domain.ExtraCharge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
@Document("buildings")
public class Building {

    @Id
    @JsonProperty
    private final String id;
    @JsonProperty
    private final String name;
    @JsonProperty
    private final String rif;
    @JsonProperty
    private final BigDecimal reserveFund;
    @JsonProperty
    private final Currency reserveFundCurrency;
    @JsonProperty
    private final Currency mainCurrency;
    @JsonProperty
    private final Currency debtCurrency;
    @JsonProperty
    private final Set<Currency> currenciesToShowAmountToPay;
    @JsonProperty
    private final List<ExtraCharge> extraCharges;
    @JsonProperty
    private final Boolean fixedPay;
    @JsonProperty
    private final BigDecimal fixedPayAmount;
    @JsonProperty
    private final ReceiptEmailFrom receiptEmailFrom;
}
