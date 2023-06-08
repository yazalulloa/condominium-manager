package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.util.LinkedHashSet;
import java.util.Optional;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.UserQueryRequest;
import kyo.yaz.condominium.manager.persistence.domain.request.UserQueryRequest.SortField;
import kyo.yaz.condominium.manager.persistence.entity.User;
import kyo.yaz.condominium.manager.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

@Service
public class UserService {

  private final UserRepository repository;

  @Autowired
  public UserService(UserRepository repository) {
    this.repository = repository;
  }

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

}
