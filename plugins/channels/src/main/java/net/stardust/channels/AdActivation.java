package net.stardust.channels;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.Communicable;
import net.stardust.base.database.crud.ChannelStatusCrud;
import net.stardust.base.database.crud.PlayerWalletCrud;
import net.stardust.base.model.channel.Ad;
import net.stardust.base.model.channel.ChannelStatus;
import net.stardust.base.model.economy.wallet.Currency;
import net.stardust.base.model.economy.wallet.Money;
import net.stardust.base.model.economy.wallet.PlayerWallet;
import net.stardust.base.utils.StardustThreads;
import net.stardust.base.utils.message.Messager;

public class AdActivation implements ChannelInventoryActivationClickHandler, Communicable {

    private Ad ad;
    private ChannelsPlugin plugin;
    private Map<UUID, NoAdCounter> counters;
    private int noAdTime;

    public AdActivation(ChannelsPlugin plugin) {
        ad = plugin.getAd();
        counters = new ConcurrentHashMap<>();
        noAdTime = plugin.getConfig().getInt("no-ad-time");
        this.plugin = plugin;
    }

    @Override
    public boolean isActivated(Player player) {
        return counters.containsKey(StardustThreads.call(plugin, () -> player.getUniqueId()));
    }

    @Override
    public void setActivated(Player player, boolean activated) {
        UUID playerId = StardustThreads.call(plugin, () -> player.getUniqueId());
        boolean op = StardustThreads.call(plugin, () -> player.isOp());
        plugin.getVirtual().submit(() -> {
            if(activated) {
                // To turn ad on there is no requirement
                if(counters.containsKey(playerId)) {
                    var counter = counters.remove(playerId);
                    if(counter != null) {
                        StardustThreads.run(plugin, counter::cancel);
                    }
                }
                ad.addParticipant(player);
                updateInDB(playerId, true);
                return;
            }
            if(op) {
                addCounter(player, playerId);
                return;
            }
            // To turn ad off the player must pay some tax
            PlayerWalletCrud crud = new PlayerWalletCrud();
            PlayerWallet wallet = crud.getOrThrow(playerId);
            Money tax = ad.getDeactivationPrice();
            Currency currency = tax.getCurrency();
            Money playerMoney = wallet.getMoney(currency);
            Messager messager = plugin.getMessager();
            BigInteger taxValue = tax.getValue();
            if(playerMoney.isSubtractionPossible(taxValue)) {
                playerMoney.subtract(taxValue);
                crud.update(wallet);
                addCounter(player, playerId);
                messager.message(player, Component.translatable("channel.ad.paid-off", NamedTextColor.GREEN, 
                    tax.toComponent(), Component.text(noAdTime, NamedTextColor.DARK_GREEN)));
                return;
            }
            // Player doesn't have money to deactivate Ad channel
            messager.message(player, Component.translatable("channel.ad.no-money-disable", NamedTextColor.RED, tax.toComponent()));
        });
    }

    private void addCounter(Player player, UUID playerId) {
        NoAdCounter counter = new NoAdCounter(playerId);
        updateInDB(playerId, false);
        ad.removeParticipant(player);
        counter.start();
        counters.put(playerId, counter);
    }

    private void updateInDB(UUID playerId, boolean activated) {
        ChannelStatusCrud crud = new ChannelStatusCrud();
        ChannelStatus status = crud.getOrThrow(playerId);
        status.getProperties().get(ad.getClass().getName()).put("status", activated);
        crud.update(status);
    }

    @RequiredArgsConstructor
    private class NoAdCounter extends BukkitRunnable {

        @NonNull
        private UUID id;

        public void start() {
            runTaskLaterAsynchronously(plugin, 20 * noAdTime);
        }

        @Override
        public void run() {
            Player player = StardustThreads.call(plugin, () -> Bukkit.getPlayer(id));
            updateInDB(id, true);
            if(player != null) {
                // Player is still online
                ad.addParticipant(player);
            }
            counters.remove(id);
        }

    }
    
}
