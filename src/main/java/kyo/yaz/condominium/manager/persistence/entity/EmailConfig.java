package kyo.yaz.condominium.manager.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
@Document("email_configs")
public class EmailConfig {


    @Id
    @JsonProperty
    private final String id;

    @JsonProperty
    private final String from;

    @JsonProperty
    private final String config;

    @JsonProperty
    private final String storedCredential;

    @JsonProperty
    private final Boolean active;

    @JsonProperty
    private final Boolean isAvailable;

    @JsonProperty
    private final String error;

    @JsonProperty
    private final ZonedDateTime createdAt;

    @JsonProperty
    private final ZonedDateTime updatedAt;

    @JsonProperty
    private final ZonedDateTime lastCheckAt;
}
