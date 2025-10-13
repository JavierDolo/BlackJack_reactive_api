package blackjack.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CardTest {
    @Test
    void valueShouldMatchRank() {
        assertThat(new Card(Card.Suit.CLUBS, Card.Rank.KING).value()).isEqualTo(10);
        assertThat(new Card(Card.Suit.HEARTS, Card.Rank.ACE).value()).isEqualTo(11);
    }
}
