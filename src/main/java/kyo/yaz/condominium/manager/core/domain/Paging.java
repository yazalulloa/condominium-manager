package kyo.yaz.condominium.manager.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Jacksonized
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@AllArgsConstructor
public class Paging<T> {
    @JsonProperty
    private final Long totalCount;
    @JsonProperty
    private final Long queryCount;
    @JsonProperty
    private final List<T> results;
}
