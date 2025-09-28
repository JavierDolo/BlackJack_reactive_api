package blackJack.model;

import java.util.Objects;

public class Card {
    public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }
    public enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7),
        EIGHT(8), NINE(9), TEN(10), JACK(10), QUEEN(10), KING(10), ACE(11);

        private final int value;
        Rank(int v) { this.value = v; }
        public int getValue() { return value; }
    }

    private Suit suit;
    private Rank rank;

    public Card() {}
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() { return suit; }
    public void setSuit(Suit suit) { this.suit = suit; }
    public Rank getRank() { return rank; }
    public void setRank(Rank rank) { this.rank = rank; }

    public int value() { return rank.getValue(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return suit == card.suit && rank == card.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }

    @Override
    public String toString() {
        return rank.name() + " of " + suit.name();
    }
}
