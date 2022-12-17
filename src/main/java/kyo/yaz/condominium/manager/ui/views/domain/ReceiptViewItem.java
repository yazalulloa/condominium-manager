package kyo.yaz.condominium.manager.ui.views.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReceiptViewItem {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String buildingId;

    @JsonProperty
    private LocalDate date;

    @JsonProperty
    private String expensesAmount;

    @JsonProperty
    private int debtReceiptsAmount;

    @JsonProperty
    private String debtAmount;

    @JsonProperty
    private String createdAt;

    @Builder.Default
    @JsonProperty
    private List<ExpenseViewItem> expenses = new ArrayList<>();

    @Builder.Default
    @JsonProperty
    private List<DebtViewItem> debts = new ArrayList<>();

}
