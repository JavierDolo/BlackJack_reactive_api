package blackJack.dto;

import jakarta.validation.constraints.NotBlank;

public class PlayerRenameRequest {
    @NotBlank
    private String newName;

    public String getNewName() { return newName; }
    public void setNewName(String newName) { this.newName = newName; }
}
