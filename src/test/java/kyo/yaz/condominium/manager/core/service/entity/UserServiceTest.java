package kyo.yaz.condominium.manager.core.service.entity;

import java.util.concurrent.TimeUnit;
import kyo.yaz.condominium.manager.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.adapter.rxjava.RxJava3Adapter;

@RunWith(SpringRunner.class)
@SpringBootTest
class UserServiceTest {

  @Autowired
  private UserRepository repository;

  @Test
  void all() throws InterruptedException {

    final var mono = repository.count();

    final var testObserver = RxJava3Adapter.monoToSingle(mono)
        .doOnSuccess(System.out::println)
        .test();

    testObserver.await(10, TimeUnit.SECONDS);

    testObserver
        .assertComplete()
        .assertNoErrors();
  }

}