package kyo.yaz.condominium.manager.core.util;

import java.util.Optional;

public class StringUtil {

    private StringUtil() {
    }

    public static Optional<String> trimFilter(String str) {
        return Optional.ofNullable(str)
                .map(String::trim)
                .filter(s -> !s.isEmpty());
    }
}
