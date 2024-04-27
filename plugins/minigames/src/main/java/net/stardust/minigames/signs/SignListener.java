package net.stardust.minigames.signs;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.stardust.base.events.BaseListener;
import net.stardust.base.minigame.Minigame;
import net.stardust.minigames.MinigamesPlugin;

@BaseListener
public class SignListener implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if(block.getState() instanceof Sign sign) {
                PersistentDataContainer container = sign.getPersistentDataContainer();
                String signData = container.get(NamespacedKey.fromString("stardust:mgsign"), PersistentDataType.STRING);
                if(signData == null) {
                    return;
                }
                String[] split = signData.split(" ");
                String minigameName = split[0];
                int index = Integer.parseInt(split[1]);
                MatchSign matchSign = MinigamesPlugin.getPlugin().getMatches().get(minigameName).get(index);
                Player player = event.getPlayer();
                if(matchSign == null) {
                    player.sendMessage(Component.translatable("internalError"));
                    throw new RuntimeException("sign without match: " + block.getLocation());
                } else {
                    switch(matchSign.getState()) {
                        case AVAILABLE -> {
                            Minigame match = matchSign.getMatch();
                            World world = match.getWorld();
                            if(world.getPlayerCount() == match.getInfo().maxPlayers()) {
                                player.sendMessage(Component.translatable("fullMatch"));
                            } else {
                                player.sendMessage(Component.translatable("entering"));
                                player.teleport(world.getSpawnLocation());
                            }
                        }
                        case RUNNING -> {
                            player.sendMessage(Component.translatable("runningMatch"));
                        }
                        case WAITING -> {
                            player.sendMessage(Component.translatable("noMatch"));
                        }
                    }
                }
            }
        }
    }

}