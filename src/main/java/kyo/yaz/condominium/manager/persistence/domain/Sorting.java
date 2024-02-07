package kyo.yaz.condominium.manager.persistence.domain;

import java.util.Objects;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Sort;

@Accessors(fluent = true)
@ToString
@Getter
public class Sorting<T extends Enum<T> & MongoSortField> {

  private final T field;

  private final Sort.Direction direction;


  public Sorting(T field, Sort.Direction direction) {
    this.field = Objects.requireNonNull(field);
    this.direction = Objects.requireNonNull(direction);
  }

  public static <S extends Enum<S> & MongoSortField> Sorting<S> of(S field, Sort.Direction direction) {
    return new Sorting<>(field, direction);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Sorting<?> sorting = (Sorting<?>) o;
    return Objects.equals(field, sorting.field);
  }

  @Override
  public int hashCode() {
    return Objects.hash(field);
  }
}

