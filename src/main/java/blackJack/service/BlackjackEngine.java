package blackJack.service;

import blackJack.model.Card;
import blackJack.model.Hand;
import blackJack.model.Outcome;

import java.util.List;

public class BlackjackEngine {

    public int total(List<Card> cards) {
        Hand h = new Hand();
        h.setCards(cards);
        return h.total();
    }

    public boolean dealerShouldHit(List<Card> dealerCards) {
        return total(dealerCards) < 17;
    }

    public Outcome decideOutcome(List<Card> player, List<Card> dealer) {
        int p = total(player);
        int d = total(dealer);
        if (p > 21) return Outcome.DEALER_WIN;
        if (d > 21) return Outcome.PLAYER_WIN;
        if (p > d) return Outcome.PLAYER_WIN;
        if (p < d) return Outcome.DEALER_WIN;
        return Outcome.PUSH;
    }
}
