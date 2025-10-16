package blackjack.controller;

import blackjack.domain.mysql.Player;
import blackjack.dto.PlayerRanking;
import blackjack.dto.PlayerRenameRequest;
import blackjack.exception.GlobalExceptionHandler;
import blackjack.service.PlayerService;
import blackjack.util.PlayerMother;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PlayerControllerTest {

    @Autowired
    WebTestClient client;

    @MockBean
    PlayerService playerService;

    @Test
    void rename_shouldReturn200() {
        Player renamed = PlayerMother.newPlayer(1L, "Renamed");
        Mockito.when(playerService.changeName(1L, "Renamed")).thenReturn(Mono.just(renamed));

        PlayerRenameRequest req = new PlayerRenameRequest();
        req.setNewName("Renamed");

        client.put().uri("/player/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Renamed");
    }

    @Test
    void ranking_shouldReturnList() {
        Mockito.when(playerService.ranking()).thenReturn(Flux.just(
                new PlayerRanking(1L, "Alice", 10, 6, 4, BigDecimal.valueOf(50)),
                new PlayerRanking(2L, "Bob", 20, 12, 8, BigDecimal.valueOf(120))
        ));

        client.get().uri("/player/ranking")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("Alice");
    }
}
