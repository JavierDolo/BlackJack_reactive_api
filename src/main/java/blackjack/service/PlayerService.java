package blackjack.service;

import blackjack.domain.mysql.Player;
import blackjack.domain.mysql.PlayerRepository;
import blackjack.dto.PlayerRanking;
import blackjack.exception.NotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class PlayerService {

    private final PlayerRepository repo;

    public PlayerService(PlayerRepository repo) {
        this.repo = repo;
    }

    public Mono<Player> findOrCreate(String name) {
        return repo.findByName(name).switchIfEmpty(repo.save(Player.of(name)));
    }

    public Mono<Player> changeName(Long playerId, String newName) {
        return repo.findById(playerId)
                .switchIfEmpty(Mono.error(new NotFoundException("Player " + playerId + " not found")))
                .flatMap(p -> {
                    p.setName(newName);
                    return repo.save(p);
                });
    }

    public Mono<Player> recordWin(Long playerId, BigDecimal amount) {
        return repo.findById(playerId).flatMap(p -> {
            p.recordWin(amount);
            return repo.save(p);
        });
    }

    public Mono<Player> recordLoss(Long playerId, BigDecimal amount) {
        return repo.findById(playerId).flatMap(p -> {
            p.recordLoss(amount);
            return repo.save(p);
        });
    }

    public Flux<PlayerRanking> ranking() {
        return repo.findTop20ByOrderByWinsDesc()
                .map(p -> new PlayerRanking(p.getId(), p.getName(), p.getGamesPlayed(), p.getWins(), p.getLosses(), p.getBalance()));
    }
}
