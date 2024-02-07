package kyo.yaz.condominium.manager.core.service.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.time.Month;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import kyo.yaz.condominium.manager.core.domain.FileResponse;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.core.service.paging.MongoServicePagingProcessorImpl;
import kyo.yaz.condominium.manager.core.service.paging.PagingJsonFile;
import kyo.yaz.condominium.manager.core.service.paging.WriteEntityToFile;
import kyo.yaz.condominium.manager.core.util.DateUtil;
import kyo.yaz.condominium.manager.persistence.domain.Sorting;
import kyo.yaz.condominium.manager.persistence.domain.request.ReceiptQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Receipt;
import kyo.yaz.condominium.manager.persistence.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

@Service("RECEIPTS")
@Slf4j
@RequiredArgsConstructor
public class ReceiptService implements MongoService<Receipt> {

  private final ObjectMapper objectMapper;
  private final ReceiptRepository repository;

  //@Cacheable("receipts-paging")
  public Single<Paging<Receipt>> paging(Set<String> buildings, Set<Month> months, String filter, int page,
      int pageSize) {

    final var sortings = new LinkedHashSet<Sorting<ReceiptQueryRequest.SortField>>();
    sortings.add(ReceiptQueryRequest.sorting(ReceiptQueryRequest.SortField.ID, Sort.Direction.DESC));

    final var request = ReceiptQueryRequest.builder()
        .buildings(buildings)
        .months(months)
        .filter(filter)
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

  public Single<List<Receipt>> list(int page, int pageSize) {
    final var mono = repository.findAllBy(PageRequest.of(page, pageSize))
        .collectList();

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Single<FileResponse> download() {

    final var writeEntityToFile = new WriteEntityToFile<>(objectMapper,
        new MongoServicePagingProcessorImpl<>(this, 20));

    return writeEntityToFile.downloadFile("receipts.json.gz");

  }

  public Single<List<Receipt>> save(Iterable<Receipt> entities) {

    final var mono = repository.saveAll(entities)
        .collectList();

    return RxJava3Adapter.monoToSingle(mono);
  }

  public Completable upload(String fileName) {
    return PagingJsonFile.pagingJsonFile(30, fileName, objectMapper, Receipt.class, c -> save(c).ignoreElement());
  }
}
