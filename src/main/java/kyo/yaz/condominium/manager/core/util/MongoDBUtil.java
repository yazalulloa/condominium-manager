package kyo.yaz.condominium.manager.core.util;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.data.mongodb.core.query.Criteria;

public class MongoDBUtil {

  private MongoDBUtil() {
  }

  public static Optional<Criteria> stringCriteria(String field, String value) {
    return StringUtil.trimFilter(value)
        .map(str -> Criteria.where(field).is(value));
  }

  public static Optional<Criteria> regexCriteria(String value, String... fields) {
    return StringUtil.trimFilter(value)
        .map(str -> {

          final var list = Arrays.stream(fields).map(field -> Criteria.where(field).regex(".*" + str + ".*", "i"))
              .toList();

          return new Criteria().orOperator(list);
        });
  }
}