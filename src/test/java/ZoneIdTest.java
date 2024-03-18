import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

public class ZoneIdTest {

  @Test
  void testZoneId() {
    System.out.println(ZoneId.systemDefault());
  }

  @Test
  void date() {

    System.out.println(OffsetDateTime.now());
    System.out.println(ZonedDateTime.now());
  }
}
