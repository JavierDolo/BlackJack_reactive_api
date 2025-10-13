package blackjack.util;

import blackjack.domain.mongo.Game;
import blackjack.model.Card;
import blackjack.model.GameStatus;
import blackjack.model.Outcome;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GameMother {

    public static Game startedGame(String id, Long playerId) {
        Game g = new Game();
        g.setId(id);
        g.setPlayerId(playerId);
        g.setDeck(new ArrayList<>(List.of(
                new Card(Card.Suit.CLUBS, Card.Rank.FIVE),
                new Card(Card.Suit.HEARTS, Card.Rank.SEVEN),
                new Card(Card.Suit.SPADES, Card.Rank.TWO),
                new Card(Card.Suit.DIAMONDS, Card.Rank.KING)
        )));
        g.setPlayerHand(new ArrayList<>(List.of(
                new Card(Card.Suit.CLUBS, Card.Rank.EIGHT),
                new Card(Card.Suit.DIAMONDS, Card.Rank.NINE)
        )));
        g.setDealerHand(new ArrayList<>(List.of(
                new Card(Card.Suit.SPADES, Card.Rank.SEVEN),
                new Card(Card.Suit.HEARTS, Card.Rank.SIX)
        )));
        g.setBet(BigDecimal.ZERO);
        g.setStatus(GameStatus.PLAYER_TURN);
        return g;
    }

    public static Game blackjackOnCreate(String id, Long playerId) {
        Game g = startedGame(id, playerId);
        g.setPlayerHand(List.of(
                new Card(Card.Suit.CLUBS, Card.Rank.ACE),
                new Card(Card.Suit.HEARTS, Card.Rank.KING)
        ));
        g.setStatus(GameStatus.FINISHED);
        g.setOutcome(Outcome.PLAYER_BLACKJACK);
        g.setBet(BigDecimal.ONE);
        return g;
    }

    public static Game bustFinished(String id, Long playerId) {
        Game g = startedGame(id, playerId);
        g.setPlayerHand(List.of(
                new Card(Card.Suit.CLUBS, Card.Rank.KING),
                new Card(Card.Suit.HEARTS, Card.Rank.QUEEN),
                new Card(Card.Suit.DIAMONDS, Card.Rank.FIVE)
        ));
        g.setStatus(GameStatus.FINISHED);
        g.setOutcome(Outcome.DEALER_WIN);
        g.setBet(BigDecimal.ONE);
        return g;
    }

    public static Game playerWinsStand(String id, Long playerId) {
        Game g = startedGame(id, playerId);
        g.setPlayerHand(List.of(
                new Card(Card.Suit.CLUBS, Card.Rank.KING),
                new Card(Card.Suit.HEARTS, Card.Rank.NINE)
        ));
        g.setDealerHand(List.of(
                new Card(Card.Suit.SPADES, Card.Rank.SEVEN),
                new Card(Card.Suit.DIAMONDS, Card.Rank.EIGHT)
        ));
        g.setStatus(GameStatus.FINISHED);
        g.setOutcome(Outcome.PLAYER_WIN);
        g.setBet(BigDecimal.ONE);
        return g;
    }
}
