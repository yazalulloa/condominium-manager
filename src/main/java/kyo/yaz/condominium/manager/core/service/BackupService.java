package kyo.yaz.condominium.manager.core.service;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kyo.yaz.condominium.manager.core.domain.FileResponse;
import kyo.yaz.condominium.manager.core.service.entity.EntityDownloader;
import kyo.yaz.condominium.manager.core.util.ZipUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BackupService {

  private final Map<String, EntityDownloader> downloaderMap;

  public Set<String> keys() {
    return downloaderMap.keySet();
  }

  public EntityDownloader get(String key) {
    return downloaderMap.get(key);
  }

  public Single<Pair<String, String>> allGz() {
    return Observable.fromIterable(downloaderMap.values())
        .map(EntityDownloader::download)
        .toList()
        .toFlowable()
        .flatMap(Single::merge)
        .toList()
        .map(list -> {

          final var set = list.stream().map(FileResponse::path).collect(Collectors.toSet());
          final var tempFile = "tmp/" + System.currentTimeMillis() + "/";
          Files.createDirectories(Paths.get(tempFile));
          final var fileName = "all.tar.gz";
          final var filePath = tempFile + fileName;
          ZipUtility.createTarGzipFiles(filePath, set);

          return Pair.of(fileName, filePath);
        });
  }
}
