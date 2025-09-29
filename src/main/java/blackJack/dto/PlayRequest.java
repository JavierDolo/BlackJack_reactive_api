package blackJack.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PlayRequest {

    public enum Action { HIT, STAND, DOUBLE }

    @NotNull
    private Action action;

    // Optional: only used on first move if you want to place a bet
    private BigDecimal bet;

    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
    public BigDecimal getBet() { return bet; }
    public void setBet(BigDecimal bet) { this.bet = bet; }
}
