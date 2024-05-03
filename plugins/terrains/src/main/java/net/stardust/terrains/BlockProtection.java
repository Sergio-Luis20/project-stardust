package net.stardust.terrains;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.ServerIdentifier;
import net.stardust.base.events.BaseListener;
import net.stardust.base.model.Identifier;
import net.stardust.base.model.user.PlayerIdentifier;
import net.stardust.base.utils.persistence.DataManager;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

@BaseListener
public class BlockProtection implements Listener {

    private TerrainsPlugin plugin;

    public BlockProtection(TerrainsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        String worldName = block.getWorld().getName();
        if(worldName.startsWith("world") || worldName.startsWith("minigame") || worldName.startsWith("dungeon")) {
            event.setCancelled(true);
            return;
        }
        if(block.getState() instanceof TileState state) {
            DataManager<TileState> manager = new DataManager<>(state);
            Player player = event.getPlayer();
            Identifier<?> identifier = plugin.getBlockServerMode().contains(player) ? 
                ServerIdentifier.INSTANCE : new PlayerIdentifier(player);
            manager.writeObject("stardust:block_owner", identifier);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        String worldName = block.getWorld().getName();
        if(worldName.startsWith("world") || worldName.startsWith("minigame") || worldName.startsWith("dungeon")) {
            event.setCancelled(true);
            return;
        }
        Player player = event.getPlayer();
        if(block.getState() instanceof TileState state) {
            DataManager<TileState> manager = new DataManager<>(state);
            Identifier<?> identifier = manager.readObject("stardust:block_owner", Identifier.class);
            if(identifier == null) {
                return;
            }
            if(identifier instanceof ServerIdentifier) {
                if(!player.isOp()) {
                    event.setCancelled(true);
                    return;
                }
                if(!plugin.getBlockServerMode().contains(player)) {
                    player.sendMessage("§c» Você deve entrar no modo servidor de configuração de bloco para destruir um bloco do servidor");
                    event.setCancelled(true);
                }
                return;
            }
            if(identifier instanceof PlayerIdentifier playerIdentifier) {
                if(!player.getUniqueId().equals(playerIdentifier.getId())) {
                    player.sendMessage(Component.translatable("block.owner.not-owner", NamedTextColor.RED));
                    event.setCancelled(true);
                }
            } else {
                player.sendMessage(Component.translatable("block.owner.not-owner", NamedTextColor.RED));
                event.setCancelled(true);
            }
        }
    }

}
