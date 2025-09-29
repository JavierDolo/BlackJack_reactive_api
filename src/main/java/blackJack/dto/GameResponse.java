package blackJack.dto;

import blackJack.model.Card;
import blackJack.model.GameStatus;
import blackJack.model.Outcome;

import java.math.BigDecimal;
import java.util.List;

public class GameResponse {
    private String id;
    private Long playerId;
    private List<Card> playerHand;
    private List<Card> dealerHand; // For simplicity, we show full dealer hand (in real game initial hidden)
    private BigDecimal bet;
    private GameStatus status;
    private Outcome outcome;
    private int playerTotal;
    private int dealerTotal;

    public GameResponse() {}

    public GameResponse(String id, Long playerId, List<Card> playerHand, List<Card> dealerHand, BigDecimal bet,
                        GameStatus status, Outcome outcome, int playerTotal, int dealerTotal) {
        this.id = id;
        this.playerId = playerId;
        this.playerHand = playerHand;
        this.dealerHand = dealerHand;
        this.bet = bet;
        this.status = status;
        this.outcome = outcome;
        this.playerTotal = playerTotal;
        this.dealerTotal = dealerTotal;
    }

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public List<Card> getPlayerHand() { return playerHand; }
    public void setPlayerHand(List<Card> playerHand) { this.playerHand = playerHand; }
    public List<Card> getDealerHand() { return dealerHand; }
    public void setDealerHand(List<Card> dealerHand) { this.dealerHand = dealerHand; }
    public BigDecimal getBet() { return bet; }
    public void setBet(BigDecimal bet) { this.bet = bet; }
    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
    public Outcome getOutcome() { return outcome; }
    public void setOutcome(Outcome outcome) { this.outcome = outcome; }
    public int getPlayerTotal() { return playerTotal; }
    public void setPlayerTotal(int playerTotal) { this.playerTotal = playerTotal; }
    public int getDealerTotal() { return dealerTotal; }
    public void setDealerTotal(int dealerTotal) { this.dealerTotal = dealerTotal; }
}
