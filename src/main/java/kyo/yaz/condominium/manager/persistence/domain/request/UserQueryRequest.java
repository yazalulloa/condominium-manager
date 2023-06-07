package kyo.yaz.condominium.manager.persistence.domain.request;

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
public class UserQueryRequest {

  private final String user;

  private final Pageable page;

  @Builder.Default
  private final Set<Sorting<SortField>> sortings = Collections.emptySet();

  public static Sorting<SortField> sorting(SortField sortField, Sort.Direction direction) {
    return new Sorting<>(sortField, direction);
  }

  public enum SortField implements MongoSortField {
    ID("_id"),
    CREATED_AT("created_at"),
    LAST_ACCESS_TOKEN_HASH_DATE("last_access_token_hash_date"),
    expirationAt("expiration_at"),
    ISSUED_AT("issued_at");

    private final String field;

    SortField(String field) {
      this.field = field;
    }

    public String field() {
      return field;
    }
  }
}
