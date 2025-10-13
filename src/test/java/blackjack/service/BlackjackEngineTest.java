package blackjack.service;

import blackjack.model.Card;
import blackjack.model.Outcome;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class BlackjackEngineTest {

    private final BlackjackEngine engine = new BlackjackEngine();

    @Test
    void shouldAdjustAceValueWhenTotalExceeds21() {
        int total = engine.total(List.of(
                new Card(Card.Suit.SPADES, Card.Rank.ACE),
                new Card(Card.Suit.HEARTS, Card.Rank.SIX),
                new Card(Card.Suit.CLUBS, Card.Rank.FIVE)
        ));
        assertThat(total).isEqualTo(12);
    }

    @Test
    void shouldHandleMultipleAcesCorrectly() {
        int total = engine.total(List.of(
                new Card(Card.Suit.SPADES, Card.Rank.ACE),
                new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                new Card(Card.Suit.CLUBS, Card.Rank.NINE)
        ));
        assertThat(total).isEqualTo(21);
    }

    @Test
    void shouldDealerHitWhenTotalIsUnder17() {
        boolean hit = engine.dealerShouldHit(List.of(
                new Card(Card.Suit.SPADES, Card.Rank.SIX),
                new Card(Card.Suit.HEARTS, Card.Rank.NINE)
        ));
        assertThat(hit).isTrue();

        boolean stand = engine.dealerShouldHit(List.of(
                new Card(Card.Suit.SPADES, Card.Rank.TEN),
                new Card(Card.Suit.HEARTS, Card.Rank.SEVEN)
        ));
        assertThat(stand).isFalse();
    }

    @Test
    void shouldDecideOutcome_PlayerBust_DealerWins() {
        Outcome outcome = engine.decideOutcome(
                List.of(
                        new Card(Card.Suit.SPADES, Card.Rank.KING),
                        new Card(Card.Suit.HEARTS, Card.Rank.QUEEN),
                        new Card(Card.Suit.CLUBS, Card.Rank.TWO)
                ),
                List.of(new Card(Card.Suit.DIAMONDS, Card.Rank.SEVEN))
        );
        assertThat(outcome).isEqualTo(Outcome.DEALER_WIN);
    }

    @Test
    void shouldDecideOutcome_DealerBust_PlayerWins() {
        Outcome outcome = engine.decideOutcome(
                List.of(
                        new Card(Card.Suit.SPADES, Card.Rank.TEN),
                        new Card(Card.Suit.HEARTS, Card.Rank.SEVEN)
                ),
                List.of(
                        new Card(Card.Suit.DIAMONDS, Card.Rank.KING),
                        new Card(Card.Suit.CLUBS, Card.Rank.QUEEN),
                        new Card(Card.Suit.HEARTS, Card.Rank.TWO)
                )
        );
        assertThat(outcome).isEqualTo(Outcome.PLAYER_WIN);
    }

    @Test
    void shouldDecideOutcome_PlayerHasHigherTotal() {
        Outcome outcome = engine.decideOutcome(
                List.of(new Card(Card.Suit.SPADES, Card.Rank.TEN)),
                List.of(new Card(Card.Suit.HEARTS, Card.Rank.NINE))
        );
        assertThat(outcome).isEqualTo(Outcome.PLAYER_WIN);
    }

    @Test
    void shouldDecideOutcome_DealerHasHigherTotal() {
        Outcome outcome = engine.decideOutcome(
                List.of(new Card(Card.Suit.SPADES, Card.Rank.NINE)),
                List.of(new Card(Card.Suit.HEARTS, Card.Rank.TEN))
        );
        assertThat(outcome).isEqualTo(Outcome.DEALER_WIN);
    }

    @Test
    void shouldDecideOutcome_PushWhenEqualTotals() {
        Outcome outcome = engine.decideOutcome(
                List.of(new Card(Card.Suit.SPADES, Card.Rank.TEN)),
                List.of(new Card(Card.Suit.HEARTS, Card.Rank.TEN))
        );
        assertThat(outcome).isEqualTo(Outcome.PUSH);
    }
}
