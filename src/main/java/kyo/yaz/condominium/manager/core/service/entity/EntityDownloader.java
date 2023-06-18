package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.core.domain.FileResponse;

public interface EntityDownloader {

  Single<FileResponse> download();
}
