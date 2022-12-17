package kyo.yaz.condominium.manager.core.util;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DateUtil {

    public static final ZoneId VE_ZONE = ZoneId.of("America/Caracas");

    private DateUtil() {
        super();
    }

    public static ZonedDateTime nowZonedWithUTC() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }
}
