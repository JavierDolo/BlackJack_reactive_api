package blackjack.service;

import blackjack.domain.mysql.Player;
import blackjack.domain.mysql.PlayerRepository;
import blackjack.exception.BadRequestException;
import blackjack.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    PlayerRepository repo;

    @InjectMocks
    PlayerService service;

    @Test
    void recordLoss_insufficientBalance() {
        Player p = Player.of("Bob");
        p.setId(1L);
        p.setBalance(BigDecimal.ZERO);

        when(repo.findById(1L)).thenReturn(Mono.just(p));

        assertThatThrownBy(() -> service.recordLoss(1L, BigDecimal.TEN).block())
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void changeName_notFound() {
        when(repo.findById(99L)).thenReturn(Mono.empty());

        assertThatThrownBy(() -> service.changeName(99L, "x").block())
                .isInstanceOf(NotFoundException.class);
    }
}
