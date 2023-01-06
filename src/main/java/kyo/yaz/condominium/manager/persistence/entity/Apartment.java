package kyo.yaz.condominium.manager.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kyo.yaz.condominium.manager.core.domain.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
@Document("apartments")
public class Apartment {
    @Id
    @JsonProperty
    private final Apartment.ApartmentId apartmentId;

    @JsonProperty
    private final String name;

    @JsonProperty
    private final String idDoc;

    @Builder.Default
    @JsonProperty
    private final Set<String> emails = Collections.emptySet();

    @JsonProperty
    private final PaymentType paymentType;

    @JsonProperty
    private final BigDecimal amountToPay;

    @Jacksonized
    @Builder(toBuilder = true)
    @Accessors(fluent = true)
    @ToString
    @Getter
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ApartmentId implements Serializable {

        @JsonProperty
        private final String buildingId;

        @JsonProperty
        private final String number;

        public static ApartmentId of(String buildingId, String number) {
            return new ApartmentId(buildingId, number);
        }
    }
}
