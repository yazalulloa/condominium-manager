package kyo.yaz.condominium.manager.ui.views.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
@Accessors(fluent = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
@AllArgsConstructor
public class User {

    private final String firstName;
    private final String lastName;
    private final String email;
    private final String picture;
}
