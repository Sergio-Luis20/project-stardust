package net.stardust.base.minigame;

import org.bukkit.entity.Player;

import java.util.List;

public interface SquadMinigame {

    List<SquadChannel> getSquadChannels();

    SquadChannel getSquadChannel(Player player);

    default void removeLeftPlayerFromSquadChannel(Player player) {
        SquadChannel squadChannel = getSquadChannel(player);
        if (squadChannel != null) {
            squadChannel.removeParticipant(player);
        }
    }

}
