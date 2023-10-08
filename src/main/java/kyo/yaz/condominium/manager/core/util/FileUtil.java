package kyo.yaz.condominium.manager.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Slf4j
public class FileUtil {


    public static File absoluteFile() {
        return Paths.get("").toAbsolutePath().toFile();
    }

    public static void showDir() {
        final var stringBuilder = new StringBuilder();
        showDir(1, absoluteFile(), stringBuilder::append);
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

        consumer.accept(" " + FileUtil.byteCountToDisplaySize(fileSize(file)));

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

    public static final long ONE_KB = 1024;

    /**
     * The number of bytes in a kilobyte.
     *
     * @since 2.4
     */
    public static final BigDecimal ONE_KB_BI = BigDecimal.valueOf(ONE_KB);

    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a megabyte.
     *
     * @since 2.4
     */
    public static final BigDecimal ONE_MB_BI = ONE_KB_BI.multiply(ONE_KB_BI);

    /**
     * The number of bytes in a gigabyte.
     */
    public static final long ONE_GB = ONE_KB * ONE_MB;

    /**
     * The number of bytes in a gigabyte.
     *
     * @since 2.4
     */
    public static final BigDecimal ONE_GB_BI = ONE_KB_BI.multiply(ONE_MB_BI);

    /**
     * The number of bytes in a terabyte.
     */
    public static final long ONE_TB = ONE_KB * ONE_GB;

    /**
     * The number of bytes in a terabyte.
     *
     * @since 2.4
     */
    public static final BigDecimal ONE_TB_BI = ONE_KB_BI.multiply(ONE_GB_BI);

    /**
     * The number of bytes in a petabyte.
     */
    public static final long ONE_PB = ONE_KB * ONE_TB;

    /**
     * The number of bytes in a petabyte.
     *
     * @since 2.4
     */
    public static final BigDecimal ONE_PB_BI = ONE_KB_BI.multiply(ONE_TB_BI);

    /**
     * The number of bytes in an exabyte.
     */
    public static final long ONE_EB = ONE_KB * ONE_PB;

    /**
     * The number of bytes in an exabyte.
     *
     * @since 2.4
     */
    public static final BigDecimal ONE_EB_BI = ONE_KB_BI.multiply(ONE_PB_BI);

    /**
     * The number of bytes in a zettabyte.
     */
    public static final BigDecimal ONE_ZB = BigDecimal.valueOf(ONE_KB).multiply(BigDecimal.valueOf(ONE_EB));

    /**
     * The number of bytes in a yottabyte.
     */
    public static final BigDecimal ONE_YB = ONE_KB_BI.multiply(ONE_ZB);

    public static final UnitSizeTuple[] UNIT_SIZE_TUPLES = {
            new UnitSizeTuple("EB", ONE_EB_BI),
            new UnitSizeTuple("PB", ONE_PB_BI),
            new UnitSizeTuple("TB", ONE_TB_BI),
            new UnitSizeTuple("GB", ONE_GB_BI),
            new UnitSizeTuple("MB", ONE_MB_BI),
            new UnitSizeTuple("KB", ONE_KB_BI)
    };


    private record UnitSizeTuple(String unit,
                                 BigDecimal size) {

    }

    public static String byteCountToDisplaySize(final BigDecimal size) {

        Objects.requireNonNull(size, "size");

        for (UnitSizeTuple tuple : UNIT_SIZE_TUPLES) {
            final var unit = tuple.unit();
            final var divisor = tuple.size();
            if (DecimalUtil.lessThan(divisor, size)) {
                final var quotient = size.divide(divisor, 4, RoundingMode.HALF_UP);
                if (DecimalUtil.greaterThan(quotient, BigDecimal.ONE)) {
                    return quotient + " " + unit;
                }
            }
        }

       /* for (UnitSizeTuple(String unit, BigDecimal divisor) : UNIT_SIZE_TUPLES) {
            if (DecimalUtil.lessThan(divisor, size)) {
                final var quotient = size.divide(divisor, 4, RoundingMode.HALF_UP);
                if (DecimalUtil.greaterThan(quotient, BigDecimal.ONE)) {
                    return quotient + " " + unit;
                }
            }
        }*/

        return size + " bytes";
    }
    public static String byteCountToDisplaySize(final long size) {
        return byteCountToDisplaySize(BigDecimal.valueOf(size));
    }


}
