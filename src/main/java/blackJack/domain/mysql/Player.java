package blackJack.domain.mysql;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("players")
public class Player {

    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("games_played")
    private int gamesPlayed;

    @Column("wins")
    private int wins;

    @Column("losses")
    private int losses;

    @Column("balance")
    private BigDecimal balance;

    @Column("created_at")
    private Instant createdAt;

    public Player() {}

    public Player(Long id, String name, int gamesPlayed, int wins, int losses, BigDecimal balance, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.losses = losses;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public static Player of(String name) {
        return new Player(null, name, 0, 0, 0, BigDecimal.ZERO, Instant.now());
    }

    // getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public void recordWin(BigDecimal amount) {
        this.wins += 1;
        this.gamesPlayed += 1;
        this.balance = this.balance.add(amount);
    }

    public void recordLoss(BigDecimal amount) {
        this.losses += 1;
        this.gamesPlayed += 1;
        this.balance = this.balance.subtract(amount);
    }
}
