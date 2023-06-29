package kyo.yaz.condominium.manager.core.util;

import java.util.stream.Stream;

public class SystemUtil {

  public static String processorsStr() {
    final var availableProcessors = Runtime.getRuntime().availableProcessors();
    return "PROCESSORS: " + availableProcessors;
  }

  public static String maxMemoryStr() {
    final var maxMemory = Runtime.getRuntime().maxMemory();
    return "MAX MEMORY: " + FileUtil.byteCountToDisplaySize(maxMemory);
  }

  public static String totalMemoryStr() {
    final var totalMemory = Runtime.getRuntime().totalMemory();
    return "TOTAL MEMORY: " + FileUtil.byteCountToDisplaySize(totalMemory);
  }

  public static String freeMemoryStr() {
    final var freeMemory = Runtime.getRuntime().freeMemory();
    return "FREE MEMORY: " + FileUtil.byteCountToDisplaySize(freeMemory);
  }

  public static String usedMemoryStr() {
    final var usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    return "USED MEMORY: " + FileUtil.byteCountToDisplaySize(usedMemory);
  }

  public static Stream<String> systemInfo() {
    final var availableProcessors = Runtime.getRuntime().availableProcessors();
    final var maxMemory = Runtime.getRuntime().maxMemory();
    final var totalMemory = Runtime.getRuntime().totalMemory();
    final var freeMemory = Runtime.getRuntime().freeMemory();
    final var usedMemory = totalMemory - freeMemory;

    return Stream.of(
        processorsStr(),
        maxMemoryStr(),
        totalMemoryStr(),
        freeMemoryStr(),
        usedMemoryStr()
    );
  }

}
