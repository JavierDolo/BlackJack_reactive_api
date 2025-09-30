package blackJack.controller;

import blackJack.domain.mongo.Game;
import blackJack.exception.BadRequestException;
import blackJack.exception.NotFoundException;
import blackJack.model.Card;
import blackJack.model.GameStatus;
import blackJack.model.Outcome;
import blackJack.service.GameService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

@WebFluxTest(controllers = GameController.class)
public class GameControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GameService gameService;

    @Test
    void newGameCreatesAndReturns201() {
        Game g = new Game();
        g.setId("game-1");
        g.setPlayerId(1L);
        g.setPlayerHand(List.of(new Card(Card.Suit.SPADES, Card.Rank.TEN), new Card(Card.Suit.HEARTS, Card.Rank.SEVEN)));
        g.setDealerHand(List.of(new Card(Card.Suit.CLUBS, Card.Rank.SIX), new Card(Card.Suit.DIAMONDS, Card.Rank.NINE)));
        g.setStatus(GameStatus.PLAYER_TURN);

        Mockito.when(gameService.createNewGame(Mockito.anyString())).thenReturn(Mono.just(g));

        String body = "{\"playerName\":\"Alice\"}";

        webTestClient.post()
                .uri("/game/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("game-1")
                .jsonPath("$.playerId").isEqualTo(1);
    }

    @Test
    void getGameReturns200WithGame() {
        Game g = new Game();
        g.setId("game-1");
        g.setPlayerId(1L);
        g.setStatus(GameStatus.PLAYER_TURN);

        Mockito.when(gameService.get("game-1")).thenReturn(Mono.just(g));

        webTestClient.get()
                .uri("/game/game-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("game-1")
                .jsonPath("$.playerId").isEqualTo(1);
    }

    @Test
    void getGameNotFoundReturns404() {
        Mockito.when(gameService.get("missing"))
                .thenReturn(Mono.error(new NotFoundException("Game missing not found")));

        webTestClient.get()
                .uri("/game/missing")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Game missing not found");
    }

    @Test
    void playWithInvalidActionReturns400() {
        Mockito.when(gameService.play(Mockito.eq("game-1"), Mockito.any()))
                .thenReturn(Mono.error(new BadRequestException("Unsupported action")));

        String body = "{\"action\":\"INVALID\"}";

        webTestClient.post()
                .uri("/game/game-1/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Unsupported action");
    }

    @Test
    void deleteGameReturns204() {
        Mockito.when(gameService.delete("game-1")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/game/game-1/delete")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void newGameWithBlackjackReturnsFinishedStatus() {
        Game g = new Game();
        g.setId("game-2");
        g.setPlayerId(1L);
        g.setStatus(GameStatus.FINISHED);
        g.setOutcome(Outcome.PLAYER_BLACKJACK);

        Mockito.when(gameService.createNewGame(Mockito.anyString()))
                .thenReturn(Mono.just(g));

        String body = "{\"playerName\":\"Alice\"}";

        webTestClient.post()
                .uri("/game/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("FINISHED")
                .jsonPath("$.outcome").isEqualTo("PLAYER_BLACKJACK");
    }

    @Test
    void playHitAndBustReturnsDealerWin() {
        Game g = new Game();
        g.setId("game-3");
        g.setPlayerId(1L);
        g.setStatus(GameStatus.FINISHED);
        g.setOutcome(Outcome.DEALER_WIN);

        Mockito.when(gameService.play(Mockito.eq("game-3"), Mockito.any()))
                .thenReturn(Mono.just(g));

        String body = "{\"action\":\"HIT\",\"bet\":5}";

        webTestClient.post()
                .uri("/game/game-3/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("FINISHED")
                .jsonPath("$.outcome").isEqualTo("DEALER_WIN");
    }

    @Test
    void playStandReturnsPlayerWin() {
        Game g = new Game();
        g.setId("game-4");
        g.setPlayerId(1L);
        g.setStatus(GameStatus.FINISHED);
        g.setOutcome(Outcome.PLAYER_WIN);

        Mockito.when(gameService.play(Mockito.eq("game-4"), Mockito.any()))
                .thenReturn(Mono.just(g));

        String body = "{\"action\":\"STAND\"}";

        webTestClient.post()
                .uri("/game/game-4/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.outcome").isEqualTo("PLAYER_WIN");
    }

    @Test
    void playDoubleReturnsFinishedGame() {
        Game g = new Game();
        g.setId("game-5");
        g.setPlayerId(1L);
        g.setStatus(GameStatus.FINISHED);
        g.setOutcome(Outcome.DEALER_WIN); // supongamos que perdió

        Mockito.when(gameService.play(Mockito.eq("game-5"), Mockito.any()))
                .thenReturn(Mono.just(g));

        String body = "{\"action\":\"DOUBLE\",\"bet\":10}";

        webTestClient.post()
                .uri("/game/game-5/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("FINISHED")
                .jsonPath("$.outcome").isEqualTo("DEALER_WIN");
    }

    @Test
    void playOnFinishedGameReturns400() {
        Mockito.when(gameService.play(Mockito.eq("game-6"), Mockito.any()))
                .thenReturn(Mono.error(new BadRequestException("Game already finished")));

        String body = "{\"action\":\"HIT\"}";

        webTestClient.post()
                .uri("/game/game-6/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Game already finished");
    }

    @Test
    void deleteNonExistingGameReturns404() {
        Mockito.when(gameService.delete("missing"))
                .thenReturn(Mono.error(new NotFoundException("Game missing not found")));

        webTestClient.delete()
                .uri("/game/missing/delete")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Game missing not found");
    }

}
