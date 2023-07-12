package kyo.yaz.condominium.manager.core.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
class SaveNewBcvRateTest {

    @Autowired
    private SaveNewBcvRate saveNewBcvRate;

    @Test
    void saveNewRate() throws InterruptedException {
        final var testObserver = saveNewBcvRate.saveNewRate()
                .repeat(10)
                .test();

        testObserver.await(120, TimeUnit.SECONDS);

        testObserver
                .assertComplete()
                .assertNoErrors();
    }
}