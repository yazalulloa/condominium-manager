package kyo.yaz.condominium.manager.core.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
        deleteDirNow("tmp");
    }

    public void deleteDirNow(String path) {
        vertx.fileSystem().exists(path)
                .flatMap(bool -> {

                    if (bool) {
                        return vertx.fileSystem().deleteRecursive(path, true);
                    }

                    return Future.succeededFuture();
                })
                .onFailure(t -> log.error("FAILED_TO_DELETE {}", path, t))
                .onSuccess(v -> log.info("PATH_DELETED: {}", path));
    }
}
