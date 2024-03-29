package kyo.yaz.condominium.manager.core.util;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class DateUtil {

  public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static final ZoneId VE_ZONE = ZoneId.of("America/Caracas");

  private DateUtil() {
    super();
  }

  public static ZonedDateTime nowZonedWithUTC() {
    return ZonedDateTime.now(ZoneOffset.UTC);
  }

  public static String format(TemporalAccessor temporalAccessor) {
    return DATE_FORMAT.format(temporalAccessor);
  }

  public static String formatVe(ZonedDateTime zonedDateTime) {
    return format(zonedDateTime.withZoneSameInstant(VE_ZONE));
  }
}
