package blackJack.controller;

import blackJack.domain.mysql.Player;
import blackJack.dto.PlayerRanking;
import blackJack.service.PlayerService;
import blackJack.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@WebFluxTest(controllers = PlayerController.class)
public class PlayerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PlayerService playerService;

    @Test
    void renamePlayerReturns200() {
        Player player = Player.of("Alice");
        player.setId(1L);
        player.setName("Bob");

        Mockito.when(playerService.changeName(Mockito.eq(1L), Mockito.eq("Bob")))
                .thenReturn(Mono.just(player));

        String body = "{\"newName\":\"Bob\"}";

        webTestClient.put()
                .uri("/player/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Bob");
    }

    @Test
    void renamePlayerNotFoundReturns404() {
        Mockito.when(playerService.changeName(Mockito.eq(99L), Mockito.anyString()))
                .thenReturn(Mono.error(new NotFoundException("Player 99 not found")));

        String body = "{\"newName\":\"Charlie\"}";

        webTestClient.put()
                .uri("/player/99")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Player 99 not found");
    }

    @Test
    void rankingReturns200WithPlayers() {
        PlayerRanking r1 = new PlayerRanking(1L, "Alice", 10, 6, 4, new BigDecimal("12.50"));
        PlayerRanking r2 = new PlayerRanking(2L, "Bob", 20, 15, 5, new BigDecimal("50.00"));

        Mockito.when(playerService.ranking()).thenReturn(Flux.just(r1, r2));

        webTestClient.get()
                .uri("/player/ranking")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Alice")
                .jsonPath("$[1].name").isEqualTo("Bob");
    }

    @Test
    void renamePlayerWithBlankNameReturns400() {
        String body = "{\"newName\":\"  \"}"; // solo espacios

        webTestClient.put()
                .uri("/player/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request");
    }

    @Test
    void rankingEmptyReturns200WithEmptyList() {
        Mockito.when(playerService.ranking()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/player/ranking")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("[]");
    }

    @Test
    void renamePlayerUnexpectedErrorReturns500() {
        Mockito.when(playerService.changeName(Mockito.eq(1L), Mockito.anyString()))
                .thenReturn(Mono.error(new RuntimeException("DB connection lost")));

        String body = "{\"newName\":\"Eve\"}";

        webTestClient.put()
                .uri("/player/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.message").value(msg -> msg.toString().contains("Unexpected error"));
    }
}
