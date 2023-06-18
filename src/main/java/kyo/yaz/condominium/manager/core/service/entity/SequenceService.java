package kyo.yaz.condominium.manager.core.service.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import kyo.yaz.condominium.manager.core.domain.FileResponse;
import kyo.yaz.condominium.manager.core.service.paging.MongoServicePagingProcessorImpl;
import kyo.yaz.condominium.manager.core.service.paging.PagingJsonFile;
import kyo.yaz.condominium.manager.core.service.paging.WriteEntityToFile;
import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import kyo.yaz.condominium.manager.persistence.repository.SequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

@Service("SEQUENCES")
@RequiredArgsConstructor
public class SequenceService implements MongoService<Sequence> {

  private final ObjectMapper objectMapper;
  private final SequenceRepository repository;


  public Single<Long> nextSequence(Sequence.Type type) {

    final var mono = repository.findById(type)
        .map(sequence -> sequence.toBuilder()
            .count(sequence.count() + 1)
            .build())
        .flatMap(repository::save)
        .switchIfEmpty(Mono.defer(() -> repository.save(new Sequence(type, 1L))))
        .map(Sequence::count);

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Single<List<Sequence>> list(int page, int pageSize) {
    final var mono = repository.findAllBy(PageRequest.of(page, pageSize))
        .collectList();

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Single<FileResponse> download() {

    final var writeEntityToFile = new WriteEntityToFile<>(objectMapper,
        new MongoServicePagingProcessorImpl<>(this, 20));

    return writeEntityToFile.downloadFile("Sequences.json.gz");

  }

  public Single<List<Sequence>> save(Iterable<Sequence> entities) {

    final var mono = repository.saveAll(entities)
        .collectList();

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Completable upload(String fileName) {
    return PagingJsonFile.pagingJsonFile(30, fileName, objectMapper, Sequence.class, c -> save(c).ignoreElement());
  }
}
