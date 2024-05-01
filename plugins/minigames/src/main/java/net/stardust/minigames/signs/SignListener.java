package net.stardust.minigames.signs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.events.BaseListener;
import net.stardust.base.minigame.Minigame;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.persistence.DataManager;
import net.stardust.minigames.MinigamesPlugin;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

@BaseListener
public class SignListener implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if(block.getState() instanceof Sign sign) {
                DataManager<Sign> manager = new DataManager<>(sign);
                MinigameSignData data = manager.readObject(MinigamesPlugin.MINIGAME_SIGN, MinigameSignData.class);
                if(data == null) {
                    return;
                }
                MatchSign matchSign = data.getMatchSign();
                Player player = event.getPlayer();
                if(matchSign == null) {
                    player.sendMessage(Component.translatable("internalError"));
                    Throwables.sendAndThrow(new SignWithoutMatchException(block.getLocation()));
                } else {
                    switch(matchSign.getState()) {
                        case AVAILABLE -> {
                            Minigame match = matchSign.getMatch();
                            World world = match.getWorld();
                            if(world.getPlayerCount() >= match.getInfo().maxPlayers()) {
                                player.sendMessage(Component.translatable("minigame.match.full", NamedTextColor.RED));
                            } else {
                                player.sendMessage(Component.translatable("minigame.match.entering", NamedTextColor.GREEN,
                                        Component.text(matchSign.getLabel(), NamedTextColor.DARK_PURPLE)));
                                player.teleport(world.getSpawnLocation());
                            }
                        }
                        case RUNNING -> player.sendMessage(Component.translatable("minigame.match.running", NamedTextColor.RED));
                        case WAITING -> player.sendMessage(Component.translatable("minigame.match.not-open-yet", NamedTextColor.RED));
                    }
                }
            }
        }
    }

}