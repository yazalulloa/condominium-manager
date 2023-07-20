package kyo.yaz.condominium.manager.core.service;

import kyo.yaz.condominium.manager.core.util.ZipUtility;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class LogService {

    public static final String ZIP_LOG_FILE_NAME = "logs.zip";
    public static final String ZIP_LOG_FILE_PATH = ZIP_LOG_FILE_NAME;

    public String zipLogs() throws IOException {
        Files.deleteIfExists(Paths.get(ZIP_LOG_FILE_PATH));
        ZipUtility.zipDirectory(new File("log"), ZIP_LOG_FILE_PATH);
        return ZIP_LOG_FILE_PATH;
    }
}
