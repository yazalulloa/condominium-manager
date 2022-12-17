package kyo.yaz.condominium.manager.core.util;

import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Optional;

public class MongoDBUtil {

    private MongoDBUtil() {
    }

    public static Optional<Criteria> stringCriteria(String field, String value) {
        return StringUtil.trimFilter(value)
                .map(str -> Criteria.where(field).is(value));
    }
}