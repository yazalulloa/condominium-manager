package kyo.yaz.condominium.manager.core.util;

import java.util.stream.Stream;

public class SystemUtil {

  public static Stream<String> systemInfo() {
    final var availableProcessors = Runtime.getRuntime().availableProcessors();
    final var maxMemory = Runtime.getRuntime().maxMemory();
    final var totalMemory = Runtime.getRuntime().totalMemory();
    final var freeMemory = Runtime.getRuntime().freeMemory();
    final var usedMemory = totalMemory - freeMemory;

    return Stream.of(
        "PROCESSORS: " + availableProcessors,
        "MAX MEMORY: " + FileUtil.byteCountToDisplaySize(maxMemory),
        "TOTAL MEMORY: " + FileUtil.byteCountToDisplaySize(totalMemory),
        "FREE MEMORY: " + FileUtil.byteCountToDisplaySize(freeMemory),
        "USED MEMORY: " + FileUtil.byteCountToDisplaySize(usedMemory)
    );
  }

}
