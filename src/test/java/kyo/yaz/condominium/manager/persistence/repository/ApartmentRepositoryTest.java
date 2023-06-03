package kyo.yaz.condominium.manager.persistence.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import kyo.yaz.condominium.manager.core.util.StringUtil;
import kyo.yaz.condominium.manager.persistence.domain.request.ApartmentQueryRequest;
import kyo.yaz.condominium.manager.persistence.entity.Apartment;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
class ApartmentRepositoryTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ApartmentRepository repository;

    @Autowired
    ReactiveMongoTemplate template;

    @Test
    void fillData() throws IOException {


        final var file = new File("C:\\Users\\Yaz\\workspace\\marlene-app\\apartments.json");

        final var tree = mapper.readTree(file);

        final var reader = mapper.readerFor(Apartment.class);

        final var apartments = new ArrayList<Apartment>();

        tree.fieldNames().forEachRemaining(key -> {

            final var arrayNode = (ArrayNode) tree.get(key);

            final var nodeIterator = arrayNode.elements();

            while (nodeIterator.hasNext()) {
                final var jsonNode = nodeIterator.next();
                try {

                    final var number = jsonNode.get("number").textValue();
                    final var apartmentId = new Apartment.ApartmentId(key, number);

                    final var apartment = reader.<Apartment>readValue(jsonNode);
                    apartments.add(apartment.toBuilder()
                            .apartmentId(apartmentId)
                            .build());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        });

      /*  repository.saveAll(apartments)
                .ignoreElements()
                .then(repository.count())
                .doOnSuccess(System.out::println)
                .block();*/

/*        final var json = mapper.writeValueAsString(apartments);

        System.out.println(json);*/

       /* final var list = repository.findAll()
                .collectList()
                .block();

        System.out.println(list);*/
    }

    @Test
    void query() {

        final var request = ApartmentQueryRequest.builder()
                .apartment("sequeraedith")
                .page(PageRequest.of(0, 20))
                .build();

        final var mono = repository.list(request)
                .doOnSuccess(apartments -> {
                    System.out.println(apartments);
                })
                .ignoreElement();

        StepVerifier.create(mono)
                .verifyComplete();

    }

    @Test
    void queryData() {

        final var request = ApartmentQueryRequest.builder()
                .apartment("fab")
                .page(PageRequest.of(0, 20))
                .build();

        final var query = new Query().with(request.page());
        final List<Criteria> criteriaList = new ArrayList<>();
        StringUtil.trimFilter(request.apartment())
                .ifPresent(str -> {
                    final var list = new ArrayList<Criteria>();

                    if (request.number() == null) {
                        list.add(Criteria.where("apartment_id.number").regex(".*" + str + ".*", "i"));
                    }

                    if (request.name() == null) {
                        list.add(Criteria.where("name").regex(".*" + str + ".*", "i"));
                    }

                    if (request.idDoc() == null) {
                        list.add(Criteria.where("id_doc").regex(".*" + str + ".*", "i"));
                    }

                    if (!list.isEmpty()) {
                        criteriaList.add(new Criteria().orOperator(list));
                    }

                });

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList));
            //query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }


        final var mono = template.find(query, Apartment.class)
                .collectList()
                .doOnSuccess(apartments -> {
                    System.out.println(apartments);
                })
                .ignoreElement();

        StepVerifier.create(mono)
                .verifyComplete();
    }

    @Test
    void queryByBuildingId() {
        final var request = ApartmentQueryRequest.builder()
                .buildings(Set.of("GLADYS"))
                .page(PageRequest.of(0, 20))
                .build();

        final var mono = repository.list(request)
                .doOnSuccess(apartments -> {
                    System.out.println(apartments);
                })
                .ignoreElement();

        StepVerifier.create(mono)
                .verifyComplete();
    }
}