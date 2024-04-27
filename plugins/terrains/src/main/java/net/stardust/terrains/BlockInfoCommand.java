package net.stardust.terrains;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.command.SenderType;
import net.stardust.base.model.Identifier;
import net.stardust.base.model.economy.sign.ShopSign;
import net.stardust.base.utils.persistence.DataManager;
import net.stardust.base.utils.plugin.PluginConfig;

@BaseCommand(value = "blockinfo", types = SenderType.PLAYER)
public class BlockInfoCommand extends DirectCommand<TerrainsPlugin> {

    private Set<UUID> players = new HashSet<>();

    public BlockInfoCommand(TerrainsPlugin plugin) {
        super(plugin);
        PluginConfig.get().registerEvents(new BlockOwnerExecution());
    }
    
    @CommandEntry
    public void blockOwner() {
        Player player = sender();
        players.add(player.getUniqueId());
        player.sendMessage(Component.translatable("block.click-block-to-view-info", NamedTextColor.GREEN));
    }

    private class BlockOwnerExecution implements Listener {
        
        @EventHandler
        public void execution(PlayerInteractEvent event) {
            Action action = event.getAction();
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            if((action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) && players.contains(playerId)) {
                Block block = event.getClickedBlock();
                BlockState state = block.getState();
                Identifier<?> ownerIdentifier = null;
                if(state instanceof TileState tileState) {
                    DataManager<TileState> dataManager = new DataManager<>(tileState);
                    ownerIdentifier = dataManager.readObject("stardust:block_owner", Identifier.class);
                }
                Component ownerName = Component.text("-", NamedTextColor.AQUA);
                if(ownerIdentifier != null) {
                    ownerName = ownerIdentifier.getComponentName().color(NamedTextColor.AQUA);
                }
                player.sendMessage(Component.translatable("block.block-info.header", NamedTextColor.GREEN));
                player.sendMessage(Component.translatable("block.block-info.owner", NamedTextColor.LIGHT_PURPLE, ownerName));
                if(state instanceof Sign sign) {
                    List<Component> messages = ShopSign.readSignToMessages(sign);
                    if(messages != null) {
                        plugin.getMessager().message(player, messages.toArray(Component[]::new));
                    }
                }
                players.remove(playerId);
            }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            players.remove(event.getPlayer().getUniqueId());
        }

    }

}
