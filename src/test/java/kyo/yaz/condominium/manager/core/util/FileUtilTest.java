package kyo.yaz.condominium.manager.core.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilTest {

    @Test
    void test() {
        final var file = Paths.get("").toAbsolutePath().toFile();
        final var freeSpace = file.getFreeSpace();
        final var usableSpace = file.getUsableSpace();
        final var totalSpace = file.getTotalSpace();



        System.out.println(FileUtil.byteCountToDisplaySize(freeSpace));
        System.out.println(FileUtil.byteCountToDisplaySize(usableSpace));
        System.out.println(FileUtil.byteCountToDisplaySize(totalSpace));
    }

}