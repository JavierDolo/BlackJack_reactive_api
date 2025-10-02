package blackjack.controller;

import blackjack.domain.mongo.Game;
import blackjack.dto.GameResponse;
import blackjack.dto.NewGameRequest;
import blackjack.dto.PlayRequest;
import blackjack.model.Hand;
import blackjack.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService service;

    public GameController(GameService service) { this.service = service; }

    @PostMapping("/new")
    public Mono<ResponseEntity<GameResponse>> newGame(@Valid @RequestBody NewGameRequest request) {
        return service.createNewGame(request.getPlayerName())
                .map(this::toResponse)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/{id}")
    public Mono<GameResponse> get(@PathVariable String id) {
        return service.get(id).map(this::toResponse);
    }

    @PostMapping("/{id}/play")
    public Mono<GameResponse> play(@PathVariable String id, @Valid @RequestBody PlayRequest request) {
        return service.play(id, request).map(this::toResponse);
    }

    @DeleteMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return service.delete(id);
    }

    private GameResponse toResponse(Game g) {
        Hand ph = new Hand(); ph.setCards(g.getPlayerHand());
        Hand dh = new Hand(); dh.setCards(g.getDealerHand());
        return new GameResponse(
                g.getId(),
                g.getPlayerId(),
                g.getPlayerHand(),
                g.getDealerHand(),
                g.getBet(),
                g.getStatus(),
                g.getOutcome(),
                ph.total(),
                dh.total()
        );
    }
}
