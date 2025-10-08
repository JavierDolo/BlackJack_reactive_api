package blackjack.controller;

import blackjack.domain.mysql.Player;
import blackjack.dto.PlayerRenameRequest;
import blackjack.dto.PlayerRanking;
import blackjack.exception.ApiError;
import blackjack.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/player")
@Tag(name = "Player", description = "Player Management and Blackjack Ranking")
public class PlayerController {

    private final PlayerService service;

    public PlayerController(PlayerService service) {
        this.service = service;
    }

    @Operation(
            summary = "Rename a player by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Player renamed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                            content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "404", description = "Player not found",
                            content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @PutMapping("/{playerId}")
    public Mono<Player> rename(@PathVariable Long playerId, @Valid @RequestBody PlayerRenameRequest request) {
        return service.changeName(playerId, request.getNewName());
    }

    @Operation(
            summary = "Get top 20 player rankings ordered by wins",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ranking retrieved successfully")
            }
    )
    @GetMapping("/ranking")
    public Flux<PlayerRanking> ranking() {
        return service.ranking();
    }
}
