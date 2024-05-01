package net.stardust.minigames.signs;

import net.stardust.minigames.MinigamesPlugin;

import java.io.Serializable;

public record MinigameSignData(String key, int index) implements Serializable {

    public MatchSign getMatchSign() {
        try {
            return MinigamesPlugin.getPlugin().getMatches().get(key).get(index - 1);
        } catch(NullPointerException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

}
