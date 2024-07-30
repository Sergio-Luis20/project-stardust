package net.stardust.channels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.SenderType;
import net.stardust.base.command.VirtualCommand;
import net.stardust.base.model.channel.Channel;
import net.stardust.base.model.channel.ChannelStatus;
import net.stardust.base.model.inventory.PseudoInventory;
import net.stardust.base.utils.ObjectMapperFactory;
import net.stardust.base.utils.StardustThreads;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.database.crud.ChannelStatusCrud;
import net.stardust.base.utils.plugin.PluginConfig;

@BaseCommand("channels")
public class ChannelsCommand extends VirtualCommand<ChannelsPlugin> implements Listener {

    private Logger logger;

    private PseudoInventory pseudoInventory;
    private Map<UUID, Inventory> inventories;
    private ChannelStatusCrud channelCrud;

    private Map<String, ChannelInventoryActivationClickHandler> activations;

    public ChannelsCommand(ChannelsPlugin plugin) {
    	super(plugin);
        PluginConfig.get().registerEvents(this);
        logger = plugin.getLogger();

        activations = new ConcurrentHashMap<>();

        activations.put(plugin.getGlobal().getName(), new GlobalActivation(plugin));
        activations.put(plugin.getAd().getName(), new AdActivation(plugin));

        pseudoInventory = getPseudoInventory();
        inventories = new ConcurrentHashMap<>();
        channelCrud = new ChannelStatusCrud();
    }
    
    @CommandEntry(types = SenderType.PLAYER)
    public void execute() {
        Player player = sender();
        UUID id = uniqueId(player);

        final Inventory inventory = StardustThreads.call(plugin, () -> Bukkit.createInventory(player, 
            pseudoInventory.getSize(), Component.translatable("word.channels")));

        pseudoInventory.getReadOnlyItems().forEach((index, pseudoItem) -> {
            if (pseudoItem == null) {
                return;
            }

            Map<String, String> labels = pseudoItem.getLabels();
            Component displayName = Component.translatable("channel." + labels.get("channelName") + ".name", NamedTextColor.DARK_AQUA);
            ChannelStatus channelStatus = channelCrud.getOrThrow(id);
            boolean status = channelStatus.isChannelActivated(labels.get("channelClassName"), "status");

            List<Component> lore = lore(status);
            
            StardustThreads.run(plugin, () -> {
                ItemStack item = new ItemStack(pseudoItem.getMaterial(), pseudoItem.getAmount());
                ItemMeta meta = item.getItemMeta();
                meta.displayName(displayName);
                meta.lore(lore);
                item.setItemMeta(meta);
                inventory.setItem(index, item);
            });
        });
        inventories.put(id, inventory);
        StardustThreads.run(plugin, () -> player.openInventory(inventory));
    }

    private PseudoInventory getPseudoInventory() {
        try {
            File inventoryPath = new File(plugin.getServerFolder(), "inventories/channel.json");
            ObjectMapper mapper = ObjectMapperFactory.getDefault();
            return mapper.readValue(inventoryPath, PseudoInventory.class);
        } catch(IOException e) {
            logger.log(Level.SEVERE, "Erro ao obter o inventário de canais");
            Throwables.sendAndThrow(getId() + "/channels-command", e);
            return null;
        }
    }

    private List<Component> lore(boolean status) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.translatable("channel." + (status ? "enabled" : "disabled"), 
            status ? NamedTextColor.GREEN : NamedTextColor.RED));
        lore.add(Component.translatable("channel.turn", NamedTextColor.BLUE, 
            Component.translatable("channel." + (status ? "disable" : "enable"), 
            status ? NamedTextColor.RED : NamedTextColor.BLUE)));
        return lore;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if(event.getWhoClicked() instanceof Player player) {
            Inventory inventory = event.getInventory();
            UUID id = player.getUniqueId();
            if(inventory.equals(inventories.get(id))) {
                event.setCancelled(true);
                ItemStack item = event.getCurrentItem();
                if(item == null) {
                    return;
                }

                String channelClassName = pseudoInventory.getReadOnlyItems().get(inventory
                    .first(item)).getLabels().get("channelClassName");

                plugin.getVirtual().submit(() -> {
                    ChannelStatus channelStatus = channelCrud.getOrThrow(id);
                    boolean status = channelStatus.isChannelActivated(channelClassName, "status");
    
                    var activation = activations.get(channelClassName);
                    synchronized(activation) {
                        activation.setActivated(player, !status);
                        status = activation.isActivated(player);
                    }
                    
                    channelCrud.update(channelStatus);
                    List<Component> lore = lore(status);

                    StardustThreads.run(plugin, () -> {
                        item.lore(lore);
                        player.playSound(player, Sound.BLOCK_BAMBOO_HIT, 1f, 1f);
                    });
                });
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if(event.getPlayer() instanceof Player player) {
            inventories.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Set<Channel> channels = plugin.getChannels();
        synchronized(channels) {
            for(Channel channel : channels) {
                var activation = activations.get(channel.getName());
                if(activation.isActivated(player)) {
                    channel.addParticipant(player);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getChannels().forEach(channel -> channel.removeParticipant(player));
        inventories.remove(player.getUniqueId());
    }

}
