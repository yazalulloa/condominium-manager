package kyo.yaz.condominium.manager.core.service;

import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileProps;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import kyo.yaz.condominium.manager.core.util.RxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeleteDirAfterDelay {

  private final Vertx vertx;
  private final int delayTime;
  private final TimeUnit delayTimeUnit;

  @Autowired
  public DeleteDirAfterDelay(Vertx vertx,
      @Value("${app.tmp_delete_delay_time}") int delayTime,
      @Value("${app.tmp_delete_delay_time_unit}") TimeUnit delayTimeUnit) {
    this.vertx = vertx;
    this.delayTime = delayTime;
    this.delayTimeUnit = delayTimeUnit;
  }

  public void deleteDir(String path) {
    deleteDir(path, delayTime, delayTimeUnit);
  }

  public void deleteDir(String path, int delayTime, TimeUnit delayTimeUnit) {

    vertx.setTimer(delayTimeUnit.toMillis(delayTime), l -> deleteDirNow(path));

  }

  public void deleteTmp() {
    deleteDir("tmp");
  }

  public void deleteTmpNow() {
    deleteDirNow("tmp");
  }

  public void deleteDirNow(String path) {

    vertx.fileSystem().exists(path)
        .filter(b -> b)
        .flatMapSingle(b -> vertx.fileSystem().props(path))
        .map(FileProps::lastModifiedTime)
        .map(lastModifiedTime -> {

          final var diff = ChronoUnit.SECONDS.between(Instant.ofEpochMilli(lastModifiedTime), Instant.now());
          return diff > 30;
        })
        .filter(b -> b)
        .flatMapCompletable(b -> vertx.fileSystem().deleteRecursive(path, true))
        .subscribe(RxUtil.completableObserver(
            () -> log.info("PATH_DELETED: {}", path),
            t -> log.error("FAILED_TO_DELETE {}", path, t)));
  }
}
