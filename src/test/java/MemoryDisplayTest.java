import java.util.stream.Stream;
import kyo.yaz.condominium.manager.core.util.FileUtil;
import org.junit.jupiter.api.Test;

public class MemoryDisplayTest {

  @Test
  void test() {
    final var maxMemory = Runtime.getRuntime().maxMemory();
    final var totalMemory = Runtime.getRuntime().totalMemory();
    final var freeMemory = Runtime.getRuntime().freeMemory();
    final var usedMemory = totalMemory - freeMemory;

    Stream.of(maxMemory, totalMemory, freeMemory, usedMemory)
        .map(FileUtil::byteCountToDisplaySize)
        .forEach(System.out::println);
  }
}
