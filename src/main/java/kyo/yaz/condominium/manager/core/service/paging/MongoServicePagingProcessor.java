package kyo.yaz.condominium.manager.core.service.paging;

import java.util.List;

public interface MongoServicePagingProcessor<T> extends PagingProcessor<List<T>> {

  @Override
  default void onTerminate() {

  }

}
