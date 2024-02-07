package kyo.yaz.condominium.manager.core.service.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.util.Comparator;
import java.util.List;
import kyo.yaz.condominium.manager.core.domain.FileResponse;
import kyo.yaz.condominium.manager.core.service.paging.MongoServicePagingProcessorImpl;
import kyo.yaz.condominium.manager.core.service.paging.PagingJsonFile;
import kyo.yaz.condominium.manager.core.service.paging.WriteEntityToFile;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;


@Service("BUILDINGS")
@RequiredArgsConstructor
@Slf4j
public class BuildingService implements MongoService<Building> {

  private final ObjectMapper objectMapper;
  private final BuildingRepository repository;


  public Single<List<Building>> list(String filter) {
    return RxJava3Adapter.monoToSingle(repository.list(filter, Pageable.unpaged()));
  }

  public Single<Building> save(Building building) {
    return RxJava3Adapter.monoToSingle(repository.save(building));
  }

  public Completable delete(Building entity) {
    return RxJava3Adapter.monoToCompletable(repository.delete(entity));
  }


  public Single<List<String>> buildingIds() {
    final var mono = repository.getIds()
        .sort(Comparator.naturalOrder())
        .collectList();

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Maybe<Building> find(String id) {
    return RxJava3Adapter.monoToMaybe(repository.findById(id));
  }

  public Single<Building> get(String id) {
    return find(id)
        .switchIfEmpty(Single.error(new RuntimeException("Building not found")));
  }

  public CompletableSource updateAptCount(String id, Long count) {

    final var mono = repository.updateAptCount(id, count);
    return RxJava3Adapter.monoToCompletable(mono);
  }

  public Single<List<Building>> list(int page, int pageSize) {
    final var mono = repository.findAllBy(PageRequest.of(page, pageSize))
        .collectList();

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Single<FileResponse> download() {

    final var writeEntityToFile = new WriteEntityToFile<>(objectMapper,
        new MongoServicePagingProcessorImpl<>(this, 20));

    return writeEntityToFile.downloadFile("buildings.json.gz");

  }

  public Single<List<Building>> save(Iterable<Building> entities) {

    final var mono = repository.saveAll(entities)
        .collectList();

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Completable upload(String fileName) {
    return PagingJsonFile.pagingJsonFile(30, fileName, objectMapper, Building.class, c -> save(c).ignoreElement());
  }
}
