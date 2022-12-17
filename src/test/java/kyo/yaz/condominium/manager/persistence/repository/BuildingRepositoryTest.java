package kyo.yaz.condominium.manager.persistence.repository;

import kyo.yaz.condominium.manager.core.service.InsertBuildings;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class BuildingRepositoryTest {

    @Autowired
    private InsertBuildings insertBuildings;

    @Autowired
    private BuildingRepository repository;

    @Test
    void fillData() {

        final var aLong = insertBuildings.insert()
                .ignoreElement()
                .then(repository.count())
                .block();

        System.out.println(aLong);

    }



}