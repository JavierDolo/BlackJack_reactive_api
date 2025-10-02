package blackjack.engine;

import blackjack.model.Card;
import blackjack.model.Outcome;
import blackjack.service.BlackjackEngine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BlackjackEngineTest {

    private final BlackjackEngine engine = new BlackjackEngine();

    @Test
    void totalsWithAce() {
        int total = engine.total(List.of(
                new Card(Card.Suit.SPADES, Card.Rank.ACE),
                new Card(Card.Suit.HEARTS, Card.Rank.SIX),
                new Card(Card.Suit.CLUBS, Card.Rank.FIVE)
        ));
        // As cuenta como 1 en este caso: 11 + 6 + 5 = 22 → ajusta a 12
        assertEquals(12, total);
    }

    @Test
    void totalsWithMultipleAces() {
        int total = engine.total(List.of(
                new Card(Card.Suit.SPADES, Card.Rank.ACE),
                new Card(Card.Suit.HEARTS, Card.Rank.ACE),
                new Card(Card.Suit.CLUBS, Card.Rank.NINE)
        ));
        // Uno de los Ases baja a 1: 11 + 1 + 9 = 21
        assertEquals(21, total);
    }

    @Test
    void dealerShouldHitUnder17() {
        assertTrue(engine.dealerShouldHit(List.of(
                new Card(Card.Suit.SPADES, Card.Rank.SIX),
                new Card(Card.Suit.HEARTS, Card.Rank.NINE)
        ))); // total 15 → debe pedir carta

        assertFalse(engine.dealerShouldHit(List.of(
                new Card(Card.Suit.SPADES, Card.Rank.TEN),
                new Card(Card.Suit.HEARTS, Card.Rank.SEVEN)
        ))); // total 17 → se planta
    }

    @Test
    void decideOutcomeScenarios() {
        // Jugador se pasa
        assertEquals(Outcome.DEALER_WIN, engine.decideOutcome(
                List.of(
                        new Card(Card.Suit.SPADES, Card.Rank.KING),
                        new Card(Card.Suit.HEARTS, Card.Rank.QUEEN),
                        new Card(Card.Suit.CLUBS, Card.Rank.TWO) // total 22
                ),
                List.of(new Card(Card.Suit.DIAMONDS, Card.Rank.SEVEN))
        ));

        // Dealer se pasa
        assertEquals(Outcome.PLAYER_WIN, engine.decideOutcome(
                List.of(
                        new Card(Card.Suit.SPADES, Card.Rank.TEN),
                        new Card(Card.Suit.HEARTS, Card.Rank.SEVEN) // total 17
                ),
                List.of(
                        new Card(Card.Suit.DIAMONDS, Card.Rank.KING),
                        new Card(Card.Suit.CLUBS, Card.Rank.QUEEN),
                        new Card(Card.Suit.HEARTS, Card.Rank.TWO) // total 22
                )
        ));

        // Jugador gana por puntos
        assertEquals(Outcome.PLAYER_WIN, engine.decideOutcome(
                List.of(new Card(Card.Suit.SPADES, Card.Rank.TEN)),   // total 10
                List.of(new Card(Card.Suit.HEARTS, Card.Rank.NINE))   // total 9
        ));

        // Dealer gana por puntos
        assertEquals(Outcome.DEALER_WIN, engine.decideOutcome(
                List.of(new Card(Card.Suit.SPADES, Card.Rank.NINE)),  // total 9
                List.of(new Card(Card.Suit.HEARTS, Card.Rank.TEN))    // total 10
        ));

        // Empate
        assertEquals(Outcome.PUSH, engine.decideOutcome(
                List.of(new Card(Card.Suit.SPADES, Card.Rank.TEN)),
                List.of(new Card(Card.Suit.HEARTS, Card.Rank.TEN))
        ));
    }
}
