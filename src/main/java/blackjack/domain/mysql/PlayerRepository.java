package blackjack.domain.mysql;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PlayerRepository extends ReactiveCrudRepository<Player, Long> {
    Mono<Player> findByName(String name);
    Flux<Player> findTop20ByOrderByWinsDesc();
}
