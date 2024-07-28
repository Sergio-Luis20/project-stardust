package net.stardust.base.model.minigame;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class MinigamePlayer implements Serializable, Comparable<MinigamePlayer> {

    private UUID id;
    private int wins, losses;

    public MinigamePlayer(UUID playerId) {
        this(playerId, 0, 0);
    }

    public MinigamePlayer(UUID playerId, int wins, int losses) {
        id = Objects.requireNonNull(playerId, "playerId");
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

    @Override
    public int compareTo(MinigamePlayer o) {
        int ofWins = wins - o.wins;
        if(ofWins == 0) {
            return o.losses - losses;
        }
        return ofWins;
    }

    public int getTotalMatches() {
        return wins + losses;
    }

    public float getRatio() {
        return (float) wins / losses;
    }

}
