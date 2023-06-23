package kyo.yaz.condominium.manager.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

@Slf4j
public class FileUtil {


  public static void showDir() {
    final var stringBuilder = new StringBuilder();
    showDir(1, Paths.get("").toAbsolutePath().toFile(), stringBuilder::append);
    log.info("\n{}", stringBuilder);
  }

  public static long fileSize(File file) {
    if (file.isDirectory()) {
      final var files = file.listFiles();
      if (files != null) {
        long fileSize = 0;
        for (File f : files) {
          fileSize += fileSize(f);
        }
        return fileSize;
      }
      return 0;
    }

    return file.length();
  }

  public static void showDir(int indent, File file, Consumer<String> consumer) {

    consumer.accept("-".repeat(indent));
    consumer.accept(" ");

    final var isDirectory = file.isDirectory();
    if (isDirectory) {
      consumer.accept("/");
    }
    consumer.accept(file.getName());

    consumer.accept(" " + FileUtils.byteCountToDisplaySize(fileSize(file)));

    consumer.accept("\n");

    if (isDirectory) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File value : files) {
          showDir(indent + 4, value, consumer);
        }
      }
    }
  }

  public static void writeEnvToFile(String env, String file) {
    Optional.ofNullable(System.getenv(env))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .ifPresent(s -> {
          try {
            Files.writeString(Paths.get(file), s);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }
}
