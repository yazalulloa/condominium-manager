package kyo.yaz.condominium.manager.core.service.entity;

import io.reactivex.rxjava3.core.Single;
import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import kyo.yaz.condominium.manager.persistence.repository.SequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

@Component
public class SequenceService {
    private final SequenceRepository repository;

    @Autowired
    public SequenceService(SequenceRepository repository) {
        this.repository = repository;
    }

    public Single<Long> rxNextSequence(Sequence.Type type) {
        return nextSequence(type);
    }

    public Single<Long> nextSequence(Sequence.Type type) {

        final var mono = repository.findById(type)
                .map(sequence -> sequence.toBuilder()
                        .count(sequence.count() + 1)
                        .build())
                .flatMap(repository::save)
                .switchIfEmpty(Mono.defer(() -> repository.save(new Sequence(type, 1L))))
                .map(Sequence::count);

        return RxJava3Adapter.monoToSingle(mono);
    }
}
