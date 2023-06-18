package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Single;
import java.util.List;

public interface MongoService<T> extends EntityDownloader {


  Single<List<T>> list(int page, int pageSize);


}
