package net.stardust.shop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.BasePlugin;
import net.stardust.base.ServerIdentifier;
import net.stardust.base.model.Identifier;
import net.stardust.base.model.IllegalIdentifierException;
import net.stardust.base.model.economy.ItemHolder;
import net.stardust.base.model.economy.sign.ShopSign;
import net.stardust.base.model.economy.sign.SignCaster;
import net.stardust.base.model.economy.sign.SignShopData;
import net.stardust.base.model.economy.sign.SignUnitData;
import net.stardust.base.model.economy.storage.ChestStorage;
import net.stardust.base.model.economy.storage.ServerStorage;
import net.stardust.base.model.economy.storage.Storage;
import net.stardust.base.model.economy.transaction.AlternativeStoragePlayerItemHolder;
import net.stardust.base.model.economy.transaction.ItemNegotiators;
import net.stardust.base.model.economy.transaction.ItemTransaction;
import net.stardust.base.model.economy.transaction.PlayerItemHolder;
import net.stardust.base.model.economy.transaction.ServerItemHolder;
import net.stardust.base.model.economy.transaction.Transaction;
import net.stardust.base.model.economy.transaction.operation.ItemNode;
import net.stardust.base.model.economy.transaction.operation.MoneyNode;
import net.stardust.base.model.economy.transaction.operation.OperationChain;
import net.stardust.base.model.economy.transaction.operation.OperationFailedException;
import net.stardust.base.model.economy.transaction.operation.SpaceNode;
import net.stardust.base.model.economy.transaction.operation.TransferNode;
import net.stardust.base.model.user.PlayerIdentifier;
import net.stardust.base.utils.persistence.DataManager;
import net.stardust.base.utils.persistence.NonRepresentativeDataException;
import net.stardust.base.utils.plugin.PluginConfig;

public class SignShopService {
    
    public static final SignShopService INSTANCE = new SignShopService();
    public static final int CONFIG_TIME;

    private Map<Player, SignShopData> configState;
    private Map<Player, BukkitRunnable> timer;

    private OperationChain operation;

    private SignShopService() {
        configState = new HashMap<>();
        timer = new HashMap<>();

        operation = new OperationChain();
        operation.addAll(new MoneyNode(), new SpaceNode(), new ItemNode(), new TransferNode());
    }

    public SignShopData putState(Player player, SignShopData data) {
        SignShopData old = configState.put(player, data);
        BukkitRunnable runnable = new BukkitRunnable() {
            
            @Override
            public void run() {
                configState.remove(player);
                timer.remove(player);
                player.sendMessage(Component.translatable("shop.sign.not-in-state-anymore", NamedTextColor.YELLOW));
            }

        };
        timer.put(player, runnable);
        runnable.runTaskLater(PluginConfig.get().getPlugin(), CONFIG_TIME * 20);
        return old;
    }

    public SignShopData removeState(Player player) {
        SignShopData data = configState.remove(player);
        BukkitRunnable runnable = timer.remove(player);
        if(runnable != null) {
            runnable.cancel();
        }
        return data;
    }

    public boolean isInConfigState(Player player) {
        return configState.containsKey(player);
    }

    public void createShopSign(Player player, Sign sign) {
        if(!isInConfigState(player)) {
            throw new NotInConfigStateException("Player: " + player.getName());
        }

        SignShopData shopData = configState.get(player);
        Identifier<?> identifier = shopData.getIdentifier();
        Storage storage;
        if(identifier instanceof PlayerIdentifier) {
            Block block = blockAttached(sign);
            if(!(block.getState() instanceof Chest chest)) {
                throw new NotAChestException("Block location: " + block.getLocation());
            }
            storage = new ChestStorage(chest);
        } else if(identifier instanceof ServerIdentifier) {
            storage = ServerStorage.INSTANCE;
        } else {
            storage = null;
        }

        if(storage == null) {
            throw new IllegalIdentifierException(identifier);
        }
        
        SignUnitData unitData = new SignUnitData(shopData, storage);
        DataManager<Sign> dataManager = new DataManager<>(sign);
        SignCaster caster = new SignCaster();

        caster.record(unitData, dataManager);
        ShopSign.writeSignText(identifier.getStringName(), sign, shopData);
        removeState(player);
    }

    public boolean isShopSign(Sign sign) {
        DataManager<Sign> dataManager = new DataManager<>(Objects.requireNonNull(sign, "sign"));
        SignCaster caster = new SignCaster();
        try {
            return caster.cast(dataManager) != null;
        } catch (NonRepresentativeDataException e) {
            return false;
        }
    }

    public void buy(Player player, Sign sign) throws NonRepresentativeDataException, OperationFailedException {
        transaction(player, sign, true);
    }

    public void sell(Player player, Sign sign) throws NonRepresentativeDataException, OperationFailedException {
        transaction(player, sign, false);
    }

    private void transaction(Player player, Sign sign, boolean isBuyer) throws NonRepresentativeDataException, OperationFailedException {
        DataManager<Sign> dataManager = new DataManager<>(sign);
        SignCaster caster = new SignCaster();
        SignUnitData signData = caster.cast(dataManager);
        SignShopData shopData = signData.shopData();

        if(isBuyer && shopData.getBuy() == null) {
            throw OperationFailedException.fromKey("cant-buy");
        } else if(!isBuyer && shopData.getSell() == null) {
            throw OperationFailedException.fromKey("cant-sell");
        }
        
        ItemHolder buyer = new PlayerItemHolder(player);
        ItemHolder seller;
        if(shopData.getIdentifier() instanceof PlayerIdentifier playerIdentifier) {
            seller = new AlternativeStoragePlayerItemHolder(playerIdentifier.getId(), signData.storage());
        } else {
            seller = ServerItemHolder.INSTANCE;
        }
        ItemNegotiators negotiators = ItemNegotiators.from(buyer, seller);

        Transaction transaction = ItemTransaction.newItemTransaction(isBuyer ? shopData.getBuy() : shopData.getSell(), 
            Arrays.asList(shopData.getItem()), isBuyer ? negotiators : negotiators.reverse());

        operation.execute(transaction);

        String key = "transaction." + (isBuyer ? "buy.bought" : "sell.sold");
        Component price = (isBuyer ? shopData.getBuy() : shopData.getSell()).toComponent();
        Identifier<?> identifier = shopData.getIdentifier();
        String name = identifier instanceof PlayerIdentifier playerIdentifier ? 
            Bukkit.getOfflinePlayer(playerIdentifier.getId()).getName() 
            : ServerIdentifier.INSTANCE.getId();
        Component otherName = Component.text(name, NamedTextColor.BLUE);

        player.sendMessage(Component.translatable(key, NamedTextColor.AQUA, ShopSign.toItemComponent(shopData.getItem()), price, otherName));
    }

    private Block blockAttached(Sign sign) {
        Block block = sign.getBlock();
        if(sign.getBlockData() instanceof WallSign wallSign) {
            return block.getRelative(wallSign.getFacing().getOppositeFace());
        }
        return block.getRelative(BlockFace.DOWN);
    }

    static {
        BasePlugin plugin = PluginConfig.get().getPlugin();
        int time = plugin.getConfig().getInt("config-time");
        if(time <= 0) {
            time = 10;
        }
        CONFIG_TIME = time;
    }

}
