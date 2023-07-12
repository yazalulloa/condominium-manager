package kyo.yaz.condominium.manager.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.persistence.domain.Debt;
import kyo.yaz.condominium.manager.persistence.domain.Expense;
import kyo.yaz.condominium.manager.persistence.domain.ExtraCharge;
import kyo.yaz.condominium.manager.persistence.domain.ReserveFund;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Document("receipts")
@EqualsAndHashCode
public class Receipt {

    @Id
    @JsonProperty
    private final Long id;
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
    private final BigDecimal totalCommonExpenses;
    @JsonProperty
    private final Currency totalCommonExpensesCurrency;
    @JsonProperty
    private final BigDecimal totalUnCommonExpenses;
    @JsonProperty
    private final Currency totalUnCommonExpensesCurrency;
    @JsonProperty
    private final List<Debt> debts;
    @JsonProperty
    private final List<AptTotal> aptTotals;
    @JsonProperty
    private final BigDecimal totalDebt;
    @JsonProperty
    private final Integer debtReceiptsAmount;
    @JsonProperty
    private final List<ExtraCharge> extraCharges;
    @JsonProperty
    private final ZonedDateTime createdAt;
    @JsonProperty
    private final ZonedDateTime updatedAt;
    @JsonProperty
    private final Rate rate;
    @JsonProperty
    private final List<ReserveFundTotal> reserveFundTotals;
    @JsonProperty
    private final Boolean sent;
    @JsonProperty
    private final ZonedDateTime lastSent;

    @Jacksonized
    @Builder(toBuilder = true)
    @Accessors(fluent = true)
    @ToString
    @Getter
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @EqualsAndHashCode
    public static class AptTotal {

        @JsonProperty
        private final String number;
        @JsonProperty
        private final String name;

        @Deprecated
        @JsonProperty
        private final BigDecimal amount;
        @JsonProperty
        private final Map<Currency, BigDecimal> amounts;
        @JsonProperty
        private final List<ExtraCharge> extraCharges;

    }

    @Jacksonized
    @Builder(toBuilder = true)
    @Accessors(fluent = true)
    @ToString
    @Getter
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @EqualsAndHashCode
    public static class ReserveFundTotal {

        @JsonProperty
        private final String name;

        @JsonProperty
        private final BigDecimal fund;

        @JsonProperty
        private final BigDecimal amount;

        @JsonProperty
        private final ReserveFund.Type type;

        @JsonProperty
        private final Expense.Type expenseType;

        @JsonProperty
        private final BigDecimal pay;

        @JsonProperty
        private final Boolean addToExpenses;

    }

}
