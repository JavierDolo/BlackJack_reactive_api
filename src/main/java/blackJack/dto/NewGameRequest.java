package blackJack.dto;

import jakarta.validation.constraints.NotBlank;

public class NewGameRequest {
    @NotBlank
    private String playerName;

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}
