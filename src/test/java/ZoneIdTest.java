import org.junit.jupiter.api.Test;

import java.time.ZoneId;

public class ZoneIdTest {

    @Test
    void testZoneId() {
        System.out.println(ZoneId.systemDefault());
    }
}
