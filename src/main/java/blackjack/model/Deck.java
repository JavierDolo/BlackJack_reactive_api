package blackjack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    public static List<Card> newShuffled() {
        List<Card> cards = new ArrayList<>(52);
        for (Card.Suit s : Card.Suit.values()) {
            for (Card.Rank r : Card.Rank.values()) {
                cards.add(new Card(s, r));
            }
        }
        Collections.shuffle(cards);
        return cards;
    }
}
