package kyo.yaz.condominium.manager.core.domain.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

import java.util.Collection;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
public class MetricResponse {

    @JsonProperty
    private final String name;
    @JsonProperty
    private final String type;
    @JsonProperty
    private final String description;
    @JsonProperty
    private final String baseUnit;
    @JsonProperty
    private final Collection<Measurement> measurements;
    @JsonProperty
    private final Collection<Tags> tags;

    @Jacksonized
    @Builder(toBuilder = true)
    @Accessors(fluent = true)
    @ToString
    @EqualsAndHashCode
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @AllArgsConstructor
    public static class Measurement {
        @JsonProperty
        private final String statistic;
        @JsonProperty
        private final double value;
    }

    @Jacksonized
    @Builder(toBuilder = true)
    @Accessors(fluent = true)
    @ToString
    @EqualsAndHashCode
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @AllArgsConstructor
    public static class Tags {
        @JsonProperty
        private final String key;
        @JsonProperty
        private final String value;
    }
}
