package kyo.yaz.condominium.manager.core.util;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import kyo.yaz.condominium.manager.core.util.rx.RetryWithDelay;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkUtil {

  public static String getPublicIp() throws IOException {
    String urlString = "http://checkip.amazonaws.com/";
    final var url = new URL(urlString);
    try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
      return br.readLine();
    }
  }

  public static void showPublicIp() {
    final var timeZone = TimeZone.getDefault();
    log.info(timeZone.toString());
    TimeZone.setDefault(timeZone);
    Single.fromCallable(NetworkUtil::getPublicIp)
        .doOnError(throwable -> log.error("FAILED_TO_GET_PUBLIC_IP", throwable))
        .retryWhen(RetryWithDelay.retry(500, TimeUnit.MILLISECONDS))
        .subscribeOn(Schedulers.io())
        .doOnSuccess(EnvUtil::saveCurrentIp)
        .subscribe(ip -> log.info("PUBLIC_IP {}", ip), throwable -> log.error("FAILED_TO_GET_PUBLIC_IP", throwable));
  }
}
