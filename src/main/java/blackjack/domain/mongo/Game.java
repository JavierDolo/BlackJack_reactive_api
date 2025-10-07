package blackjack.domain.mongo;

import blackjack.model.Card;
import blackjack.model.GameStatus;
import blackjack.model.Outcome;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document("games")
public class Game {

    @Id
    private String id;

    @Indexed
    private Long playerId;

    private List<Card> deck = new ArrayList<>();
    private List<Card> playerHand = new ArrayList<>();
    private List<Card> dealerHand = new ArrayList<>();

    private BigDecimal bet = BigDecimal.ZERO;

    private GameStatus status = GameStatus.PLAYER_TURN;
    private Outcome outcome;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Game() {}

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public List<Card> getDeck() { return deck; }
    public void setDeck(List<Card> deck) { this.deck = deck; }
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
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
