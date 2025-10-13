package blackjack.controller;

import blackjack.domain.mongo.Game;
import blackjack.dto.NewGameRequest;
import blackjack.dto.PlayRequest;
import blackjack.exception.GlobalExceptionHandler;
import blackjack.service.GameService;
import blackjack.util.GameMother;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;

@WebFluxTest(controllers = GameController.class)
@Import(GlobalExceptionHandler.class)
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
})
class GameControllerTest {

    @Autowired
    WebTestClient client;

    @MockBean
    GameService gameService;

    @Test
    void createNewGame_shouldReturn201() {
        Game g = GameMother.startedGame("g1", 1L);
        Mockito.when(gameService.createNewGame("Alice")).thenReturn(Mono.just(g));

        NewGameRequest req = new NewGameRequest();
        req.setPlayerName("Alice");

        client.post().uri("/game/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("g1")
                .jsonPath("$.playerId").isEqualTo(1)
                .jsonPath("$.status").isEqualTo("PLAYER_TURN");
    }

    @Test
    void createGameWithBlackjack_shouldReturnBlackjackOutcome() {
        Game g = GameMother.blackjackOnCreate("bj1", 1L);
        Mockito.when(gameService.createNewGame("BJ")).thenReturn(Mono.just(g));

        NewGameRequest req = new NewGameRequest();
        req.setPlayerName("BJ");

        client.post().uri("/game/new")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.outcome").isEqualTo("PLAYER_BLACKJACK")
                .jsonPath("$.status").isEqualTo("FINISHED");
    }

    @Test
    void getGameById_shouldReturn200() {
        Game g = GameMother.startedGame("g1", 2L);
        Mockito.when(gameService.get("g1")).thenReturn(Mono.just(g));

        client.get().uri("/game/g1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("g1")
                .jsonPath("$.playerId").isEqualTo(2);
    }

    @Test
    void playValidAction_shouldReturn200() {
        Game after = GameMother.startedGame("g1", 1L);
        Mockito.when(gameService.play(eq("g1"), any(PlayRequest.class))).thenReturn(Mono.just(after));

        PlayRequest pr = new PlayRequest();
        pr.setAction(PlayRequest.Action.HIT);

        client.post().uri("/game/g1/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pr)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("g1");
    }

    @Test
    void invalidAction_shouldReturn400() {
        Mockito.when(gameService.play(eq("g1"), any(PlayRequest.class)))
                .thenReturn(Mono.error(new blackjack.exception.BadRequestException("Unsupported action")));

        PlayRequest pr = new PlayRequest();
        pr.setAction(PlayRequest.Action.HIT);

        client.post().uri("/game/g1/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pr)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void simulateBust_dealerWins() {
        Game g = GameMother.bustFinished("g2", 1L);
        Mockito.when(gameService.play(eq("g2"), any(PlayRequest.class))).thenReturn(Mono.just(g));

        PlayRequest pr = new PlayRequest();
        pr.setAction(PlayRequest.Action.HIT);

        client.post().uri("/game/g2/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pr)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.outcome").isEqualTo("DEALER_WIN")
                .jsonPath("$.status").isEqualTo("FINISHED");
    }

    @Test
    void simulateStand_playerWins() {
        Game g = GameMother.playerWinsStand("g3", 1L);
        Mockito.when(gameService.play(eq("g3"), any(PlayRequest.class))).thenReturn(Mono.just(g));

        PlayRequest pr = new PlayRequest();
        pr.setAction(PlayRequest.Action.STAND);

        client.post().uri("/game/g3/play")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pr)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.outcome").isEqualTo("PLAYER_WIN")
                .jsonPath("$.status").isEqualTo("FINISHED");
    }

    @Test
    void deleteGame_shouldReturn204() {
        Mockito.when(gameService.delete("g9")).thenReturn(Mono.empty());

        client.delete().uri("/game/g9/delete")
                .exchange()
                .expectStatus().isNoContent();
    }
}
