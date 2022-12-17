package kyo.yaz.condominium.manager.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kyo.yaz.condominium.manager.core.domain.Currency;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Document("rates")
public class Rate {

    @Id
    @JsonProperty
    private Long id;

    @JsonProperty
    private Currency fromCurrency;

    @JsonProperty
    private Currency toCurrency;

    @JsonProperty
    private BigDecimal rate;

    @JsonProperty
    private BigDecimal roundedRate;

    @JsonProperty
    private LocalDate dateOfRate;

    @JsonProperty
    private Source source;

    @JsonProperty
    private ZonedDateTime createdAt;


    public enum Source {
        BCV, PLATFORM;

        public static final Source[] values = values();
    }
}
