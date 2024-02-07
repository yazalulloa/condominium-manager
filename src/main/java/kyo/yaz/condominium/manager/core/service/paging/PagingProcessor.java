package kyo.yaz.condominium.manager.core.service.paging;

import io.reactivex.rxjava3.core.Single;

public interface PagingProcessor<T> {

  Single<T> next();

  boolean isComplete();

  void onTerminate();
}
