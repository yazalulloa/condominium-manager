package kyo.yaz.condominium.manager.core.service;

import kyo.yaz.condominium.manager.core.domain.Currency;
import kyo.yaz.condominium.manager.persistence.entity.Building;
import kyo.yaz.condominium.manager.persistence.repository.BuildingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Service
public class InsertBuildings {

    private final BuildingRepository repository;

    @Autowired
    public InsertBuildings(BuildingRepository repository) {
        this.repository = repository;
    }

    public Mono<Void> insert() {
        final var antonieta = Building.builder()
                .id("ANTONIETA")
                .name("RESIDENCIAS ANTONIETA")
                .rif("J-31430722-3")
                .mainCurrency(Currency.VED)
                .build();

        final var gladys = Building.builder()
                .id("GLADYS")
                .name("RESIDENCIAS GLADYS")
                .rif("J-31049453-3")
                .mainCurrency(Currency.VED)
                .build();

        final var inoa = Building.builder()
                .id("INOA")
                .name("RESIDENCIAS INOA")
                .rif("J-31428505-0")
                .mainCurrency(Currency.USD)
                .build();

        final var koral = Building.builder()
                .id("KORAL")
                .name("RESIDENCIAS KORAL")
                .rif("J-30869674-9")
                .mainCurrency(Currency.VED)
                .currenciesToShowAmountToPay(Set.of(Currency.USD, Currency.VED))
                .build();

        final var maracaibo = Building.builder()
                .id("MARACAIBO")
                .name("RESIDENCIAS MARACAIBO")
                .rif("J-31423279-7")
                .mainCurrency(Currency.VED)
                .build();

        final var marahuaka = Building.builder()
                .id("MARAHUAKA")
                .name("RESIDENCIAS MARAHUAKA")
                .rif("J-31437926-7")
                .mainCurrency(Currency.USD)
                .build();

        final var mendi = Building.builder()
                .id("MENDI")
                .name("EDIFICIO MENDI-EDER C.")
                .rif("J-31613621-3")
                .mainCurrency(Currency.VED)
                .build();

        final var tulipanes = Building.builder()
                .id("TULIPANES")
                .name("EDIFICIO I. LOS TULIPANES")
                .rif("J-31424284-9")
                .mainCurrency(Currency.VED)
                .build();


        final var stream = List.of(antonieta, gladys, inoa, koral, maracaibo, marahuaka, mendi, tulipanes);


        return repository.insert(stream)
                .then();
    }
}
