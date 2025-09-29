package blackJack.dto;

import java.math.BigDecimal;

public class PlayerRanking {
    private Long id;
    private String name;
    private int gamesPlayed;
    private int wins;
    private int losses;
    private BigDecimal balance;
    private double winRate;

    public PlayerRanking() {}

    public PlayerRanking(Long id, String name, int gamesPlayed, int wins, int losses, BigDecimal balance) {
        this.id = id;
        this.name = name;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.losses = losses;
        this.balance = balance;
        this.winRate = gamesPlayed == 0 ? 0.0 : (wins * 100.0) / gamesPlayed;
    }

    // getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public int getGamesPlayed() { return gamesPlayed; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public BigDecimal getBalance() { return balance; }
    public double getWinRate() { return winRate; }
}
