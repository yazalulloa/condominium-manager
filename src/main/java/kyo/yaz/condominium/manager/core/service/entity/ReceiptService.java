package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.time.Month;
import java.util.LinkedHashSet;
import java.util.Set;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.ReceiptQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.repository.ReceiptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ReceiptService {

  private final ReceiptRepository repository;

  @Autowired
  public ReceiptService(ReceiptRepository repository) {
    this.repository = repository;
  }

  public Single<Paging<Receipt>> paging(Set<String> buildings, Set<Month> months, String filter, int page,
      int pageSize) {

    final var sortings = new LinkedHashSet<Sorting<ReceiptQueryRequest.SortField>>();
    sortings.add(ReceiptQueryRequest.sorting(ReceiptQueryRequest.SortField.ID, Sort.Direction.DESC));

    final var request = ReceiptQueryRequest.builder()
        .buildings(buildings)
        .months(months)
        .expense(filter)
        .page(PageRequest.of(page, pageSize))
        .sortings(sortings)
        .build();

    return RxJava3Adapter.monoToSingle(paging(request));
  }

  private Mono<Paging<Receipt>> paging(ReceiptQueryRequest request) {
    final var listMono = repository.list(request);
    final var totalCountMono = repository.count();
    final var queryCountMono = repository.count(request);

    return Mono.zip(totalCountMono, queryCountMono, listMono)
        .map(tuple -> new Paging<>(tuple.getT1(), tuple.getT2(), tuple.getT3()));
  }

  public Completable delete(Long id) {
    return RxJava3Adapter.monoToCompletable(repository.deleteById(id));
  }

  public Completable delete(Receipt entity) {

    return RxJava3Adapter.monoToCompletable(repository.delete(entity));
  }

  public Maybe<Receipt> find(Long id) {
    return RxJava3Adapter.monoToMaybe(repository.findById(id));
  }

  public Single<Receipt> get(Long id) {
    return find(id)
        .switchIfEmpty(Single.error(new RuntimeException("Receipt not found")));
  }

  public Single<Receipt> save(Receipt receipt) {
    return RxJava3Adapter.monoToSingle(repository.save(receipt));
  }

  public Single<Long> countAll() {
    return RxJava3Adapter.monoToSingle(repository.count());
  }


  public Completable updateSent(Receipt receipt) {
    final var build = receipt.toBuilder()
        .sent(true)
        .lastSent(DateUtil.nowZonedWithUTC())
        .build();

    return save(build).ignoreElement();
  }
}
