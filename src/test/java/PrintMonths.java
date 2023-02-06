import org.junit.jupiter.api.Test;

import java.time.Month;

public class PrintMonths {

    @Test
    void test() {
        for (final Month month : Month.values()) {
            System.out.println(month + "=" + month);
        }
    }
}
