package kyo.yaz.condominium.manager.core.service.entity;

import kyo.yaz.condominium.manager.persistence.entity.Sequence;
import kyo.yaz.condominium.manager.persistence.repository.SequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SequenceService {
    private final SequenceRepository repository;

    @Autowired
    public SequenceService(SequenceRepository repository) {
        this.repository = repository;
    }

    public Mono<Long> nextSequence(Sequence.Type type) {
        return repository.findById(type)
                .map(sequence -> sequence.toBuilder()
                        .count(sequence.count() + 1)
                        .build())
                .flatMap(repository::save)
                .switchIfEmpty(Mono.defer(() -> repository.save(new Sequence(type, 1L))))
                .map(Sequence::count);
    }
}
