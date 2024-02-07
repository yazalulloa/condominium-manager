import java.time.Month;
import org.junit.jupiter.api.Test;

public class PrintMonths {

  @Test
  void test() {
    for (final Month month : Month.values()) {
      System.out.println(month + "=" + month);
    }
  }
}
