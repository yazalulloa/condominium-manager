import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;

public class RoundingTest {

  @Test
  void round() {
    final var decimal = BigDecimal.valueOf(4.35);

    System.out.println(decimal.setScale(1, RoundingMode.UP));
  }
}
