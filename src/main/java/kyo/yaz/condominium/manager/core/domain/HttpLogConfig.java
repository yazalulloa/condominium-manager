package kyo.yaz.condominium.manager.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.HashSet;
import java.util.Set;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
public class HttpLogConfig {
    @Builder.Default
    @JsonProperty("exclude_query_params")
    private final Set<String> excludeQueryParams = new HashSet<>();

    @Builder.Default
    @JsonProperty("exclude_headers")
    private final Set<String> excludeHeaders = new HashSet<>();

    @Builder.Default
    @JsonProperty("exclude_fields")
    private final Set<String> excludeFields = new HashSet<>();

    @Builder.Default
    @JsonProperty("show_body")
    private final boolean showBody = true;

    @Builder.Default
    @JsonProperty("max_size_body")
    private final int maxSizeBody = 10000;

    @Builder.Default
    @JsonProperty("pretty")
    private final boolean pretty = true;
}
