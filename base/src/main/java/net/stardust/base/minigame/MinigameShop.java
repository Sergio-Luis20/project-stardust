package net.stardust.base.minigame;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.stardust.base.events.WorldListener;
import net.stardust.base.minigame.Minigame.MinigameState;
import net.stardust.base.model.economy.transaction.*;
import net.stardust.base.model.economy.transaction.operation.*;
import net.stardust.base.model.economy.wallet.Currency;
import net.stardust.base.model.economy.wallet.Money;
import net.stardust.base.utils.SoundPack;
import net.stardust.base.utils.database.crud.PlayerWalletCrud;
import net.stardust.base.utils.inventory.CantStoreItemsException;
import net.stardust.base.utils.inventory.InventoryUtils;
import net.stardust.base.utils.item.ItemUtils;
import net.stardust.base.utils.persistence.DataManager;
import net.stardust.base.utils.ranges.Ranges;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

@Getter
public class MinigameShop extends WorldListener {

    private static final NamespacedKey SHOP_ITEM_KEY, SHOP_BOOK_KEY;
    private static final SoundPack BUY_SOUND;
    private static final ItemStack SHOP_BOOK;
    private static PlayerWalletCrud crud = new PlayerWalletCrud();
    private static Operation buyItem;

    private Minigame parent;
    private Inventory inventory;
    private boolean preMatchOnly;

    private MinigameShop(Minigame parent, Inventory inventory, boolean preMatchOnly) {
        super(parent::getWorld);
        this.parent = Objects.requireNonNull(parent, "parent");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.preMatchOnly = preMatchOnly;
    }

    public static MinigameShop newShop(Minigame parent, boolean preMatchOnly, Component inventoryTitle,
                                       int inventorySize, Map<Integer, ItemStack> items) {
        inventorySize = Ranges.rangeInclusive(inventorySize, 9, 9 * 6, "inventorySize");
        if(inventorySize % 9 != 0) {
            throw new IllegalArgumentException("inventorySize must be a multiple of 9");
        }
        for(Entry<Integer, ItemStack> entry : Objects.requireNonNull(items, "items").entrySet()) {
            Ranges.rangeInclusive(Objects.requireNonNull(entry.getKey(),
                    "index in map items"), 0 , inventorySize - 1, "index in map items");
            Objects.requireNonNull(entry.getValue(), "value in map items");
        }
        MinigameShopInventoryHolder holder = new MinigameShopInventoryHolder();
        Inventory inventory = Bukkit.createInventory(holder, inventorySize, inventoryTitle);
        holder.setInventory(inventory);
        items.forEach(inventory::setItem);
        return new MinigameShop(parent, inventory, preMatchOnly);
    }

    public static void giveShopBook(Player player) throws CantStoreItemsException {
        PlayerInventory inventory = player.getInventory();
        boolean set = false;
        for(int i = 8; i >= 0; i--) {
            ItemStack item = inventory.getItem(i);
            if(item == null || item.getType() == Material.AIR) {
                inventory.setItem(i, SHOP_BOOK);
                set = true;
                break;
            }
        }
        if(!set) {
            if(!InventoryUtils.canStoreAllItems(inventory, SHOP_BOOK)) {
                throw new CantStoreItemsException(inventory, player);
            }
            inventory.addItem(SHOP_BOOK);
        }
    }

    @EventHandler
    public void onIteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(player.getWorld().equals(parent.getWorld())) {
            Action action = event.getAction();
            if(action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            ItemStack item = event.getItem();
            if(item == null || !isShopBook(item)) {
                return;
            }
            if(!preMatchOnly || parent.getState() == MinigameState.PRE_MATCH) {
                event.getPlayer().openInventory(inventory);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if(inventory.getHolder() instanceof MinigameShopInventoryHolder) {
            event.setCancelled(true);
            if(event.getWhoClicked() instanceof Player player && player.getWorld().equals(parent.getWorld())
                    && (!preMatchOnly || parent.getState() == MinigameState.PRE_MATCH)) {
                ItemStack clickedItem = event.getCurrentItem();
                if(clickedItem == null || clickedItem.getType() == Material.AIR) {
                    return;
                }
                ItemMeta meta = clickedItem.getItemMeta();
                DataManager<ItemMeta> manager = new DataManager<>(meta);
                Money price = manager.readObject(SHOP_ITEM_KEY, Money.class);
                if(price == null) {
                    throw new MinigameShopItemWithoutPriceException("Minigame: " + parent.getInfo().name());
                }
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
                BUY_SOUND.play(player);
                player.sendMessage(message);
                parent.getWorld().getPlayers().forEach(p -> p.sendMessage(othersMessage));
            }
        }
    }

    public static ItemStack createShopItem(ItemStack item, int price) {
        ItemStack shopItem = ItemUtils.item(Objects.requireNonNull(item, "item"));
        Ranges.greater(price, 0, "price");
        Money money = new Money(Currency.SILVER, new BigInteger(String.valueOf(price)));
        ItemMeta meta = shopItem.getItemMeta();
        DataManager<ItemMeta> manager = new DataManager<>(meta);
        manager.writeObject(SHOP_ITEM_KEY, money);
        Component priceComponent = Component.text("Price: ", NamedTextColor.AQUA)
                .append(money.toComponent());
        meta.lore(List.of(priceComponent));
        shopItem.setItemMeta(meta);
        return shopItem;
    }

    public static boolean isShopBook(ItemStack item) {
        DataManager<ItemMeta> manager = new DataManager<>(item.getItemMeta());
        return manager.readBoolean(SHOP_BOOK_KEY);
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
        SHOP_ITEM_KEY = new NamespacedKey("stardust", "minigame_shop_item_price");
        SHOP_BOOK_KEY = new NamespacedKey("stardust", "minigame_shop_book");
        BUY_SOUND = new SoundPack(Sound.ENTITY_PLAYER_BURP, 1, 0.5f);
        SHOP_BOOK = new ItemStack(Material.WRITABLE_BOOK);

        ItemMeta meta = SHOP_BOOK.getItemMeta();
        meta.displayName(Component.text("Shop", NamedTextColor.GREEN, TextDecoration.ITALIC));
        DataManager<ItemMeta> manager = new DataManager<>(meta);
        manager.writeObject(SHOP_BOOK_KEY, true);
        SHOP_BOOK.setItemMeta(meta);

        OperationChain chain = new OperationChain();
        chain.addAll(new MoneyNode(), new SpaceNode(), new TransferNode());
        buyItem = chain;
    }

}
