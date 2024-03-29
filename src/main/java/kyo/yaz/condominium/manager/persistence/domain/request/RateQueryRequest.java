package kyo.yaz.condominium.manager.persistence.domain.request;

import java.util.Collections;
import java.util.Set;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.persistence.domain.MongoSortField;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.entity.Rate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Builder(toBuilder = true)
@Accessors(fluent = true)
@Data
public class RateQueryRequest {

  private final Set<Long> ids;
  private final Set<Long> hashes;
  private final Set<Currency> fromCurrency;
  private final Set<Currency> toCurrency;
  private final Set<Rate.Source> source;

  private final Pageable page;

  @Builder.Default
  private final Set<Sorting<SortField>> sortings = Collections.emptySet();

  public static Sorting<RateQueryRequest.SortField> sorting(SortField sortField, Sort.Direction direction) {
    return new Sorting<>(sortField, direction);
  }

  public enum SortField implements MongoSortField {
    ID("id"),
    RATE("rate"),
    ROUNDED_RATE("rounded_rate"),
    DATE_OF_RATE("date_of_rate"),
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
