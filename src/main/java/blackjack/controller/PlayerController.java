package blackjack.controller;

import blackjack.domain.mysql.Player;
import blackjack.dto.PlayerRenameRequest;
import blackjack.dto.PlayerRanking;
import blackjack.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/player")
public class PlayerController {

    private final PlayerService service;

    public PlayerController(PlayerService service) { this.service = service; }

    @PutMapping("/{playerId}")
    public Mono<Player> rename(@PathVariable Long playerId, @Valid @RequestBody PlayerRenameRequest request) {
        return service.changeName(playerId, request.getNewName());
    }

    @GetMapping("/ranking")
    public Flux<PlayerRanking> ranking() {
        return service.ranking();
    }
}
