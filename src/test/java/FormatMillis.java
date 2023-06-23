import java.time.Instant;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.jupiter.api.Test;

public class FormatMillis {

  @Test
  void format() {

    //var before = 1687492356388L;
    var before = 1687392356388L;
    final var currentTimeMillis = System.currentTimeMillis();

    final var timeUp = currentTimeMillis - before;
    System.out.println(timeUp);
    System.out.println(timeUp / 1000);

    final var formatDuration = DurationFormatUtils.formatDuration(timeUp, "HH:mm:ss", false);
    System.out.println(formatDuration);
    System.out.println(Instant.ofEpochMilli(before).atZone(DateUtil.VE_ZONE));
  }

}
