package kyo.yaz.condominium.manager.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class StringUtil {

    private StringUtil() {
    }

    public static Optional<String> trimFilter(String str) {
        return Optional.ofNullable(str)
                .map(String::trim)
                .filter(s -> !s.isEmpty());
    }

    public static byte[] compress(byte[] bytes) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(os)) {
            dos.write(bytes);
        }
        return os.toByteArray();
    }

    public static byte[] decompress(byte[] bytes) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (OutputStream ios = new InflaterOutputStream(os)) {
            ios.write(bytes);
        }

        return os.toByteArray();
    }

    public static String compressStr(String str) throws IOException {
        final var bytes = compress(str.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    public static String unCompressStr(String str) throws IOException {
        final var decode = Base64.getUrlDecoder().decode(str);
        final var decompress = decompress(decode);
        return new String(decompress, StandardCharsets.UTF_8);
    }
}
