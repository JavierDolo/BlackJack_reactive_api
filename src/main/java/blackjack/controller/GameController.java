package blackjack.controller;

import blackjack.domain.mongo.Game;
import blackjack.dto.GameResponse;
import blackjack.dto.NewGameRequest;
import blackjack.dto.PlayRequest;
import blackjack.exception.ApiError;
import blackjack.model.Hand;
import blackjack.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @Operation(
            summary = "Create a new Blackjack game",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Game created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @PostMapping("/new")
    public Mono<ResponseEntity<GameResponse>> newGame(@Valid @RequestBody NewGameRequest request) {
        return service.createNewGame(request.getPlayerName())
                .map(this::toResponse)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @Operation(
            summary = "Get an existing Blackjack game by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Game retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Game not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @GetMapping("/{id}")
    public Mono<GameResponse> get(@PathVariable String id) {
        return service.get(id).map(this::toResponse);
    }

    @Operation(
            summary = "Play a move in an active Blackjack game",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Move applied successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid move", content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "404", description = "Game not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @PostMapping("/{id}/play")
    public Mono<GameResponse> play(@PathVariable String id, @Valid @RequestBody PlayRequest request) {
        return service.play(id, request).map(this::toResponse);
    }

    @Operation(
            summary = "Delete a Blackjack game by ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Game deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Game not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @DeleteMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return service.delete(id);
    }

    private GameResponse toResponse(Game g) {
        Hand ph = new Hand();
        ph.setCards(g.getPlayerHand());
        Hand dh = new Hand();
        dh.setCards(g.getDealerHand());
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
