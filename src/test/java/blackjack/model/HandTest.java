package blackjack.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class HandTest {
    @Test
    void aceAs11Or1AndBlackjackBust() {
        Hand h = new Hand();
        h.add(new Card(Card.Suit.SPADES, Card.Rank.ACE));
        h.add(new Card(Card.Suit.HEARTS, Card.Rank.NINE));
        assertThat(h.total()).isEqualTo(20);

        h.add(new Card(Card.Suit.CLUBS, Card.Rank.FIVE));
        assertThat(h.total()).isEqualTo(15);

        Hand bj = new Hand();
        bj.add(new Card(Card.Suit.CLUBS, Card.Rank.ACE));
        bj.add(new Card(Card.Suit.DIAMONDS, Card.Rank.KING));
        assertThat(bj.isBlackjack()).isTrue();
    }
}
