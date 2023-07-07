import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundingTest {

    @Test
    void round() {
        final var decimal = BigDecimal.valueOf(4.35);

        System.out.println(decimal.setScale(1, RoundingMode.UP));
    }
}
