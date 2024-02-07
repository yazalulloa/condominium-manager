package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.json.Json;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import kyo.yaz.condominium.manager.core.domain.Paging;
import kyo.yaz.condominium.manager.ui.views.util.AppUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class ApartmentServiceTest {

  @Autowired
  private ApartmentService service;

  @Test
  void paging() throws Exception {

    final var single = service.paging(Set.of("MARACAIBO"), null, 0, 100);

    final var testObserver = single.map(Paging::results)
        .flatMapObservable(Observable::fromIterable)
        .sorted(Comparator.comparing(o -> o.apartmentId().number()))
        .toList()
        .doOnSuccess(list -> {
          list.forEach(a -> System.out.println(Json.encode(a)));
        })
        .test();

    testObserver.await(10, TimeUnit.SECONDS);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }

  @Test
  void modifyMaracaibo() throws InterruptedException {

    final var testObserver = service.paging(Set.of("MARACAIBO"), null, 0, 100)
        .map(Paging::results)
        .flatMapObservable(Observable::fromIterable)
        .filter(apartment -> AppUtil.isNumeric(apartment.apartmentId().number()))
        .filter(apartment -> apartment.apartmentId().number().length() == 1)
        /* .map(apartment -> apartment.toBuilder()
                 .apartmentId(apartment.apartmentId().toBuilder()
                         .number("0" + apartment.apartmentId().number())
                         .build())
                 .build())*/
        .toList()
        .flatMapCompletable(service::delete)
        /* .doOnSuccess(list -> {
             list.forEach(a -> System.out.println(Json.encode(a)));
         })*/
        .test();

    testObserver.await(10, TimeUnit.SECONDS);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }
}