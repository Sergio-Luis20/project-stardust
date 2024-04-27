package net.stardust.base.minigame;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.minigame.Minigame.MinigameState;
import net.stardust.base.model.economy.transaction.*;
import net.stardust.base.model.economy.transaction.operation.*;
import net.stardust.base.model.economy.wallet.Currency;
import net.stardust.base.model.economy.wallet.Money;
import net.stardust.base.utils.ItemUtils;
import net.stardust.base.utils.SoundPack;
import net.stardust.base.utils.database.crud.PlayerWalletCrud;
import net.stardust.base.utils.persistence.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;

@Getter
public class MinigameShop implements Listener {

    private static final NamespacedKey SHOP_ITEM = new NamespacedKey("stardust", "minigame_shop_item_price");
    private static final SoundPack sound = new SoundPack(Sound.ENTITY_PLAYER_BURP, 1, 0.5f);
    private static PlayerWalletCrud crud = new PlayerWalletCrud();
    private static Operation buyItem;

    private Minigame parent;
    private Inventory inventory;
    private boolean preMatchOnly;

    private MinigameShop(Minigame parent, Inventory inventory, boolean preMatchOnly) {
        this.parent = Objects.requireNonNull(parent, "parent");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.preMatchOnly = preMatchOnly;
    }

    public static MinigameShop newShop(Minigame parent, boolean preMatchOnly, Component inventoryTitle,
                                       int inventorySize, Map<Integer, ItemStack> items) {
        var holder = new MinigameShopInventoryHolder();
        Inventory inventory = Bukkit.createInventory(holder, inventorySize, inventoryTitle);
        holder.setInventory(inventory);
        items.forEach(inventory::setItem);
        return new MinigameShop(parent, inventory, preMatchOnly);
    }

    public void openShop(Player player) {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if(inventory.getHolder() instanceof MinigameShopInventoryHolder) {
            event.setCancelled(true);
            if(event.getWhoClicked() instanceof Player player && (!preMatchOnly || parent.getState() == MinigameState.PRE_MATCH)) {
                ItemStack clickedItem = event.getCurrentItem();
                ItemMeta meta = clickedItem.getItemMeta();
                DataManager<ItemMeta> manager = new DataManager<>(meta);
                Money price = manager.readObject(SHOP_ITEM, Money.class);
                assert price != null : "Item without price in minigame shop inventory";
                Transaction transaction = ItemTransaction.newItemTransaction(price,
                        List.of(clickedItem), ItemNegotiators.from(new PlayerItemHolder(player),
                                ServerItemHolder.INSTANCE));
                try {
                    transaction.performOperation(buyItem);
                } catch (OperationFailedException e) {
                    player.sendMessage(e.getDefaultFailMessage(true));
                    return;
                }
                Component priceComponent = price.toComponent();
                Component message = Component.translatable("minigame.bought.self",
                        NamedTextColor.GREEN, priceComponent);
                Component othersMessage = Component.translatable("minigame.bought.others",
                        NamedTextColor.AQUA, Component.text(player.getName(), NamedTextColor.GOLD),
                        priceComponent);
                sound.play(player);
                player.sendMessage(message);
                parent.getWorld().getPlayers().forEach(p -> p.sendMessage(othersMessage));
            }
        }
    }

    public ItemStack createShopItem(ItemStack item, int price) {
        ItemStack shopItem = ItemUtils.item(Objects.requireNonNull(item, "item"));
        if(price < 0) {
            throw new IllegalArgumentException("price must be positive");
        }
        Money money = new Money(Currency.SILVER, new BigInteger(String.valueOf(price)));
        ItemMeta meta = shopItem.getItemMeta();
        DataManager<ItemMeta> manager = new DataManager<>(meta);
        manager.writeObject(SHOP_ITEM, money);
        Component priceComponent = Component.text("Price: ", NamedTextColor.AQUA)
                .append(money.toComponent());
        meta.lore(List.of(priceComponent));
        shopItem.setItemMeta(meta);
        return shopItem;
    }

    public static class MinigameShopInventoryHolder implements InventoryHolder {

        private Inventory inventory;

        private void setInventory(Inventory inventory) {
            this.inventory = Objects.requireNonNull(inventory, "inventory");
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }

    }

    static {
        OperationChain chain = new OperationChain();
        chain.addAll(new MoneyNode(), new SpaceNode(), new TransferNode());
        buyItem = chain;
    }

}
