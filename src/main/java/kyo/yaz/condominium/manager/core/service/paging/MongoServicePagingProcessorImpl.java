package kyo.yaz.condominium.manager.core.service.paging;

import io.reactivex.rxjava3.core.Single;
import java.util.List;
import kyo.yaz.condominium.manager.core.service.entity.MongoService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoServicePagingProcessorImpl<T> implements MongoServicePagingProcessor<T> {

  private final MongoService<T> service;
  private final int pageSize;
  private volatile boolean isComplete;
  private int page;

  public MongoServicePagingProcessorImpl(MongoService<T> service, int pageSize) {
    this.service = service;
    this.pageSize = pageSize;
  }

  @Override
  public Single<List<T>> next() {
    return service.list(page, pageSize)
        .doOnSuccess(list -> {
          //log.info("PAGE {} SIZE {}", page, list.size());
          page++;
          isComplete = list.isEmpty();
        });
  }

  @Override
  public boolean isComplete() {
    return isComplete;
  }
}
