package kyo.yaz.condominium.manager.core.service.paging;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;
import kyo.yaz.condominium.manager.core.domain.FileResponse;
import kyo.yaz.condominium.manager.core.util.RxUtil;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class WriteEntityToFile<T> {

  private final ObjectMapper mapper;
  private final MongoServicePagingProcessor<T> pagingProcessor;

  public Single<FileResponse> downloadFile(String fileName) {

    return Single.defer(() -> {

      final var temPath = "tmp/" + System.currentTimeMillis() + "/";
      Files.createDirectories(Paths.get(temPath));
      final var tempFileName = temPath + fileName;

      final var fileOutputStream = new FileOutputStream(tempFileName);
      final var gzipOutputStream = new GZIPOutputStream(fileOutputStream);
      final var jsonGenerator = mapper.getFactory().createGenerator(gzipOutputStream, JsonEncoding.UTF8);

      jsonGenerator.writeStartArray();

      return RxUtil.paging(pagingProcessor, collection -> Observable.fromIterable(collection)
              .doOnNext(obj -> mapper.writeValue(jsonGenerator, obj))
              .ignoreElements())
          .doOnComplete(jsonGenerator::writeEndArray)
          .doOnTerminate(jsonGenerator::close)
          .doOnTerminate(gzipOutputStream::close)
          .doOnTerminate(fileOutputStream::close)
          .toSingleDefault(FileResponse.builder()
              .fileName(fileName)
              .path(tempFileName)
              .contentType("application/gzip")
              .build());
    });
  }
}
