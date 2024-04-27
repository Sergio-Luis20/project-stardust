package net.stardust.base.minigame;

import org.bukkit.entity.Player;

import java.util.List;

public interface SquadMinigame {

    List<SquadChannel> getSquadChannels();
    SquadChannel getSquadChannel(Player player);

}
