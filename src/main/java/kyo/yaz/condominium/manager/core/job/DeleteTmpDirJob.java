package kyo.yaz.condominium.manager.core.job;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DeleteTmpDirJob {

    private final Vertx vertx;

    @Autowired
    public DeleteTmpDirJob(Vertx vertx) {
        this.vertx = vertx;
    }

    @Async
    @Scheduled(initialDelay = 5, fixedRate = 86400, timeUnit = TimeUnit.SECONDS)
    public void runAsStart() {

        final var path = "tmp";

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
