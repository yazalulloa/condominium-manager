package kyo.yaz.condominium.manager.persistence.domain.request;

import java.util.Collections;
import java.util.Set;
import kyo.yaz.condominium.manager.core.domain.PaymentType;
import kyo.yaz.condominium.manager.persistence.domain.MongoSortField;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
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
public class ApartmentQueryRequest {

  private final Set<String> buildings;

  private final String number;

  private final String name;

  private final String idDoc;

  private final String email;

  private final String apartment;

  private final PaymentType paymentType;

  private final Pageable page;

  @Builder.Default
  private final Set<Sorting<SortField>> sortings = Collections.emptySet();

  public static Sorting<SortField> sorting(SortField sortField, Sort.Direction direction) {
    return new Sorting<>(sortField, direction);
  }

  public enum SortField implements MongoSortField {
    BUILDING_ID("_id.building_id"),
    NUMBER("_id.number"),
    NAME("name"),
    ID_DOC("id_doc"),
    AMOUNT_TO_PAY("amount_to_pay");

    private final String field;

    SortField(String field) {
      this.field = field;
    }

    public String field() {
      return field;
    }
  }
}
