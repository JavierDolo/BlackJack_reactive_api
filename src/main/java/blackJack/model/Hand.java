package blackJack.model;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    private List<Card> cards = new ArrayList<>();

    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> cards) { this.cards = cards; }

    public void add(Card c) { cards.add(c); }

    public int total() {
        int sum = 0;
        int aces = 0;
        for (Card c : cards) {
            sum += c.value();
            if (c.getRank() == Card.Rank.ACE) aces++;
        }
        while (sum > 21 && aces > 0) {
            sum -= 10; // Treat an Ace as 1 instead of 11
            aces--;
        }
        return sum;
    }

    public boolean isBlackjack() { return cards.size() == 2 && total() == 21; }
    public boolean isBust() { return total() > 21; }
}
