package kyo.yaz.condominium.manager.persistence.domain.request;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.Set;
import kyo.yaz.condominium.manager.persistence.domain.MongoSortField;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Builder(toBuilder = true)
@Accessors(fluent = true)
@ToString
@Getter
@AllArgsConstructor
public class ReceiptQueryRequest {

  private final Set<String> buildings;
  private final Set<Month> months;
  private final LocalDate date;
  private final String filter;
  private final Pageable page;
  @Builder.Default
  private final Set<Sorting<SortField>> sortings = Collections.emptySet();
  private Long id;

  public static Sorting<SortField> sorting(SortField sortField, Sort.Direction direction) {
    return new Sorting<>(sortField, direction);
  }

  public enum SortField implements MongoSortField {
    ID("id"),
    BUILDING_ID("building_id"),
    DATE("date");

    private final String field;

    SortField(String field) {
      this.field = field;
    }

    public String field() {
      return field;
    }
  }
}
