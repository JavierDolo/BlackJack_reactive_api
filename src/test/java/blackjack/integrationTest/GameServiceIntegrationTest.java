package blackjack.integrationTest;

import blackjack.dto.GameResponse;
import blackjack.dto.NewGameRequest;
import blackjack.dto.PlayRequest;
import blackjack.model.GameStatus;
import blackjack.model.Outcome;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class GameServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    // === Testcontainers ===
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8")
            .withDatabaseName("blackjack")
            .withUsername("root")
            .withPassword("root");

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);

        registry.add("spring.r2dbc.url", () ->
                "r2dbc:mysql://" + mysql.getHost() + ":" + mysql.getMappedPort(3306) + "/blackjack");
        registry.add("spring.r2dbc.username", mysql::getUsername);
        registry.add("spring.r2dbc.password", mysql::getPassword);

        registry.add("spring.flyway.url", () ->
                "jdbc:mysql://" + mysql.getHost() + ":" + mysql.getMappedPort(3306) + "/blackjack");
        registry.add("spring.flyway.user", mysql::getUsername);
        registry.add("spring.flyway.password", mysql::getPassword);
    }

    private WebTestClient client() {
        return webTestClient.mutate()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void createNewGame_shouldDealCards() {
        NewGameRequest req = new NewGameRequest();
        req.setPlayerName("Alice");

        GameResponse resp = client().post().uri("/game/new")
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(GameResponse.class)
                .returnResult().getResponseBody();

        assertThat(resp).isNotNull();
        assertThat(resp.getPlayerHand()).hasSize(2);
        assertThat(resp.getDealerHand()).hasSize(2);
        assertThat(resp.getStatus()).isIn(GameStatus.PLAYER_TURN, GameStatus.FINISHED);
    }

    @Test
    void hit_shouldAddCardOrFinishGame() {
        NewGameRequest req = new NewGameRequest();
        req.setPlayerName("Bob");
        GameResponse game = client().post().uri("/game/new")
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(GameResponse.class)
                .returnResult().getResponseBody();

        assertThat(game).isNotNull();

        if (game.getStatus() == GameStatus.FINISHED) {
            // juego acabado de inicio (ej. blackjack)
            assertThat(game.getOutcome()).isNotNull();
            return;
        }

        PlayRequest play = new PlayRequest();
        play.setAction(PlayRequest.Action.HIT);

        GameResponse afterHit = client().post().uri("/game/{id}/play", game.getId())
                .bodyValue(play)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameResponse.class)
                .returnResult().getResponseBody();

        assertThat(afterHit).isNotNull();
        assertThat(afterHit.getPlayerHand().size()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void stand_shouldFinishGameOrAlreadyFinished() {
        NewGameRequest req = new NewGameRequest();
        req.setPlayerName("Charlie");
        GameResponse game = client().post().uri("/game/new")
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(GameResponse.class)
                .returnResult().getResponseBody();

        assertThat(game).isNotNull();

        if (game.getStatus() == GameStatus.FINISHED) {
            assertThat(game.getOutcome()).isNotNull();
            return;
        }

        PlayRequest play = new PlayRequest();
        play.setAction(PlayRequest.Action.STAND);

        GameResponse finished = client().post().uri("/game/{id}/play", game.getId())
                .bodyValue(play)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameResponse.class)
                .returnResult().getResponseBody();

        assertThat(finished).isNotNull();
        assertThat(finished.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(finished.getOutcome()).isIn(
                Outcome.PLAYER_WIN, Outcome.DEALER_WIN, Outcome.PUSH
        );
    }

    @Test
    void double_shouldDoubleBetOrAlreadyFinished() {
        NewGameRequest req = new NewGameRequest();
        req.setPlayerName("Diana");
        GameResponse game = client().post().uri("/game/new")
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(GameResponse.class)
                .returnResult().getResponseBody();

        assertThat(game).isNotNull();

        if (game.getStatus() == GameStatus.FINISHED) {
            assertThat(game.getOutcome()).isNotNull();
            return;
        }

        PlayRequest play = new PlayRequest();
        play.setAction(PlayRequest.Action.DOUBLE);

        GameResponse finished = client().post().uri("/game/{id}/play", game.getId())
                .bodyValue(play)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GameResponse.class)
                .returnResult().getResponseBody();

        assertThat(finished).isNotNull();
        assertThat(finished.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(finished.getBet().intValue()).isEqualTo(2);
    }
}
