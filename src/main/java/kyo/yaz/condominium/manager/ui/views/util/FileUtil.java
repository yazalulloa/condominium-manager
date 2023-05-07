package kyo.yaz.condominium.manager.ui.views.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.function.Consumer;

@Slf4j
public class FileUtil {

    public static void showDir() {
        final var stringBuilder = new StringBuilder();
        showDir(1, Paths.get("").toAbsolutePath().toFile(), stringBuilder::append);
        log.info("\n{}", stringBuilder);
    }

    public static void showDir(int indent, File file, Consumer<String> consumer) {

        consumer.accept("-".repeat(indent));
        consumer.accept(" ");

        if (file.isDirectory()) {
            consumer.accept("/");
        }
        consumer.accept(file.getName());
        consumer.accept("\n");

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File value : files) showDir(indent + 4, value, consumer);
            }
        }
    }
}
