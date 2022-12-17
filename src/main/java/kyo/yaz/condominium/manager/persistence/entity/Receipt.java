package kyo.yaz.condominium.manager.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.List;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Document("receipts")
public class Receipt {

    @Id
    @JsonProperty
    private Long id;

    @JsonProperty
    private final String buildingId;

    @JsonProperty
    private final Integer year;

    @JsonProperty
    private final Month month;

    @JsonProperty
    private final LocalDate date;

    @JsonProperty
    private final List<Expense> expenses;

    @JsonProperty
    private final List<Debt> debts;


    @JsonProperty
    private final ZonedDateTime createdAt;

    @JsonProperty
    private final ZonedDateTime updatedAt;


    @JsonProperty
    private Long rateId;

    @JsonProperty
    private BigDecimal rate;

    @JsonProperty
    private BigDecimal roundedRate;

    @JsonProperty
    private LocalDate dateOfRate;

    @JsonProperty
    private Rate.Source source;

}
