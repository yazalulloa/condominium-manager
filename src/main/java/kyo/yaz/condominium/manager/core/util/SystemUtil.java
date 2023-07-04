package kyo.yaz.condominium.manager.core.util;

import java.util.stream.Stream;

public class SystemUtil {


    public static String ipStr() {
        return "IP: " + EnvUtil.currentIp();
    }
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


    public static String freeSpaceStr() {
        final var size = FileUtil.absoluteFile().getFreeSpace();
        return "FREE SPACE: " + FileUtil.byteCountToDisplaySize(size);
    }

    public static String usableSpaceStr() {
        final var size = FileUtil.absoluteFile().getUsableSpace();
        return "USABLE SPACE: " + FileUtil.byteCountToDisplaySize(size);
    }

    public static String totalSpaceStr() {
        final var size = FileUtil.absoluteFile().getTotalSpace();
        return "TOTAL SPACE: " + FileUtil.byteCountToDisplaySize(size);
    }

    public static String usedSpaceStr() {
        final var file = FileUtil.absoluteFile();
        final var size = file.getTotalSpace() - file.getFreeSpace();
        return "USED SPACE: " + FileUtil.byteCountToDisplaySize(size);
    }


    public static Stream<String> systemInfo() {

        return Stream.of(
                processorsStr(),
                maxMemoryStr(),
                totalMemoryStr(),
                freeMemoryStr(),
                usedMemoryStr(),
                freeSpaceStr(),
                usableSpaceStr(),
                totalSpaceStr(),
                usedSpaceStr()
        );
    }

}
