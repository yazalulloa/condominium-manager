package kyo.yaz.condominium.manager.persistence.domain.request;

import kyo.yaz.condominium.manager.persistence.domain.MongoSortField;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.Set;

@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@AllArgsConstructor
public class EmailConfigQueryRequest {

    private final String filter;

    private final String isAvailable;
    private final Pageable page;

    @Builder.Default
    private final Set<Sorting<SortField>> sortings = Collections.emptySet();

    public static Sorting<SortField> sorting(SortField sortField, Sort.Direction direction) {
        return new Sorting<>(sortField, direction);
    }

    public enum SortField implements MongoSortField {
        ID("id"),
        CREATED_AT("created_at");

        private final String field;

        SortField(String field) {
            this.field = field;
        }

        public String field() {
            return field;
        }
    }
}
