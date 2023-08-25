package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.core.json.Json;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.core.service.paging.MongoServicePagingProcessorImpl;
import kyo.yaz.condominium.manager.core.util.RxUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class RateServiceTest {

  @Autowired
  private RateService service;

  @Test
  void last() throws InterruptedException {

    final var longs = Set.of(1701662066L, 3519043957L);

    final var testObserver = Observable.fromIterable(longs)
        .flatMapMaybe(hash -> service.last(Currency.USD, Currency.VED))
        .map(Json::encode)
        .doOnNext(System.out::println)
        .test();

    testObserver.await(60, TimeUnit.SECONDS);

    testObserver
        .assertComplete()
        .assertNoErrors();

  }


  @Test
  void download() throws InterruptedException {

    final var download = service.download();

    final var testObserver = download
        .doOnSuccess(System.out::println)
        //.flatMapCompletable(service::upload)
        .test();

    testObserver.await(120, TimeUnit.SECONDS);

    testObserver
        .assertComplete()
        .assertNoErrors();

  }

  @Test
  void upload() throws InterruptedException {

    final var upload = service.upload("rates.json.gz");

    final var testObserver = upload
        .andThen(service.countAll())
        .doOnSuccess(System.out::println)
        //.flatMapCompletable(service::upload)
        .test();

    testObserver.await(120, TimeUnit.SECONDS);

    testObserver
        .assertComplete()
        .assertNoErrors();

  }

  @Test
  void delete() throws InterruptedException {
    final var i = 261;

    final var sets = IntStream.range(i, i + 20)
        .mapToObj(Long::valueOf)
        .collect(Collectors.toSet());

    final var testObserver = service.delete(sets)
        .test();

    testObserver.await(120, TimeUnit.SECONDS);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }

  @Test
  void fixHashesAndEtags() throws InterruptedException {

    final var processor = new MongoServicePagingProcessorImpl<>(service, 30);

    final var testObserver = RxUtil.paging(processor, collection -> Observable.fromIterable(collection)
            .flatMapMaybe(rate -> {

              if (
                  (rate.hashes() != null && rate.hashes().isEmpty())
                      || rate.hash() == null
              ) {
                return Maybe.empty();
              }

              final var hashes = Optional.of(rate.hash())
                  .map(Set::of)
                  .orElse(null);

              final var etags = Optional.ofNullable(rate.etag())
                  .map(Set::of)
                  .orElse(null);

              final var build = rate.toBuilder()
                  .hashes(hashes)
                  .etags(etags)
                  .build();

              return Maybe.just(build);
            })
            .toList()
            .flatMap(service::save)
            .ignoreElement())
        .test();

    testObserver.await(5, TimeUnit.MINUTES);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }

  @Test
  void exists() throws InterruptedException {
    final var testObserver = service.exists(123123L)
        .doOnSuccess(System.out::println)
        .test();

    testObserver.await(5, TimeUnit.MINUTES);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }
}