package blackjack.util;

import blackjack.domain.mysql.Player;

import java.math.BigDecimal;
import java.time.Instant;

public class PlayerMother {
    public static Player newPlayer(Long id, String name) {
        return new Player(id, name, 0, 0, 0, BigDecimal.ZERO, Instant.now());
    }
}
