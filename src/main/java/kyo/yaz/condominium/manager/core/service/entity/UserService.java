package kyo.yaz.condominium.manager.core.service.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import kyo.yaz.condominium.manager.core.domain.FileResponse;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.service.paging.MongoServicePagingProcessorImpl;
import kyo.yaz.condominium.manager.core.service.paging.PagingJsonFile;
import kyo.yaz.condominium.manager.core.service.paging.WriteEntityToFile;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.UserQueryRequest;
import kyo.yaz.condominium.manager.persistence.domain.request.UserQueryRequest.SortField;
import kyo.yaz.condominium.manager.persistence.entity.User;
import kyo.yaz.condominium.manager.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

@Service("USERS")
@RequiredArgsConstructor
public class UserService implements MongoService<User> {

  private final ObjectMapper objectMapper;
  private final UserRepository repository;

  public Maybe<User> maybe(String id) {
    final var mono = repository.findById(id);
    return RxJava3Adapter.monoToMaybe(mono);
  }

  public Single<Optional<User>> find(String id) {
    return maybe(id).map(Optional::of)
        .defaultIfEmpty(Optional.empty());
  }

  public Completable delete(User entity) {
    return RxJava3Adapter.monoToCompletable(repository.delete(entity));
  }

  public Single<User> save(User entity) {
    return RxJava3Adapter.monoToSingle(repository.save(entity));
  }

  public Single<Paging<User>> paging(String filter, int page, int pageSize) {

    final var sortings = new LinkedHashSet<Sorting<SortField>>();

    sortings.add(UserQueryRequest.sorting(SortField.CREATED_AT, Direction.DESC));

    final var request = UserQueryRequest.builder()
        .user(filter)
        .page(PageRequest.of(page, pageSize))
        .sortings(sortings)
        .build();

    return RxJava3Adapter.monoToSingle(paging(request));
  }

  private Mono<Paging<User>> paging(UserQueryRequest request) {
    final var listMono = repository.list(request);
    final var totalCountMono = repository.count();
    final var queryCountMono = repository.count(request);

    return Mono.zip(totalCountMono, queryCountMono, listMono)
        .map(tuple -> new Paging<>(tuple.getT1(), tuple.getT2(), tuple.getT3()));
  }

  public Single<List<User>> list(int page, int pageSize) {
    final var mono = repository.findAllBy(PageRequest.of(page, pageSize))
        .collectList();

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Single<FileResponse> download() {

    final var writeEntityToFile = new WriteEntityToFile<>(objectMapper,
        new MongoServicePagingProcessorImpl<>(this, 20));

    return writeEntityToFile.downloadFile("users.json.gz");

  }

  public Single<List<User>> save(Iterable<User> entities) {

    final var mono = repository.saveAll(entities)
        .collectList();

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Completable upload(String fileName) {
    return PagingJsonFile.pagingJsonFile(30, fileName, objectMapper, User.class, c -> save(c).ignoreElement());
  }
}
