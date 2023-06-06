package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.vertx.core.json.Json;
import kyo.yaz.condominium.manager.persistence.entity.EmailConfig;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
class EmailConfigServiceTest {

    @Autowired
    private EmailConfigService service;

    @Test
    void listForComboBox() throws InterruptedException {

        final var single = service.listForComboBox();

        final var testObserver = single.map(Json::encode)
                .doOnSuccess(System.out::println)
                .test();

        testObserver.await(10, TimeUnit.SECONDS);

        testObserver
                .assertComplete()
                .assertNoErrors();

    }

}