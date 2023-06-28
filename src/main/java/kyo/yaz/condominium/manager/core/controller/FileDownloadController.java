package kyo.yaz.condominium.manager.core.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(FileDownloadController.PATH)
@RequiredArgsConstructor
@Slf4j
public class FileDownloadController {

  public static final String PATH = "/file-download";

  @Async
  @GetMapping()
  public CompletableFuture<ResponseEntity<InputStreamResource>> fileDownload() throws FileNotFoundException {
    log.info("FILE_DOWNLOAD {}", Thread.currentThread());
    //vertx.eventBus().sender(TelegramVerticle.WEB_HOOK).write(json);

    final var downloadFile = new File("log/application.log");

    final var in = new BufferedInputStream(new FileInputStream(downloadFile));
    final var responseEntity = ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + downloadFile.getName())
        .header("Cache-Control", "no-cache, no-store, must-revalidate")
        .header("Pragma", "no-cache")
        .header("Expires", "0")
        .contentLength(downloadFile.length())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(new InputStreamResource(in));
    return CompletableFuture.completedFuture(responseEntity);
  }
}
