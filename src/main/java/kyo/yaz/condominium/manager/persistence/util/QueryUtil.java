package kyo.yaz.condominium.manager.persistence.util;

import kyo.yaz.condominium.manager.persistence.domain.MongoSortField;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Optional;
import java.util.Set;

public class QueryUtil {

    public static <T extends Enum<T> & MongoSortField> void addSortings(Query query, Set<Sorting<T>> sortings) {
        Optional.of(sortings)
                .filter(s -> !s.isEmpty())
                .map(set -> {
                    return set.stream().map(sorting -> new Sort.Order(sorting.direction(), sorting.field().field()))
                            .toList();
                })
                .map(Sort::by)
                .ifPresent(query::with);

    }
}
