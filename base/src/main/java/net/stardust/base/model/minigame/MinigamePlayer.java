package net.stardust.base.model.minigame;

import java.util.Objects;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MinigamePlayer {

    private UUID id;
    private int wins, losses;

    public MinigamePlayer(UUID playerId) {
        this(playerId, 0, 0);
    }

    public MinigamePlayer(UUID playerId, int wins, int losses) {
        this.id = Objects.requireNonNull(id, "id");
        setWins(wins);
        setLosses(losses);
    }

    public void setWins(int wins) {
        if(wins < 0) {
            throw new IllegalArgumentException("wins < 0");
        }
        this.wins = wins;
    }

    public void setLosses(int losses) {
        if(losses < 0) {
            throw new IllegalArgumentException("loses < 0");
        }
        this.losses = losses;
    }

    public int getTotalMatches() {
        return wins + losses;
    }

    public double getRatio() {
        return losses == 0 ? wins : (double) wins / losses;
    }

}
