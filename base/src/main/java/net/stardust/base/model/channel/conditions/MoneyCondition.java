package net.stardust.base.model.channel.conditions;

import java.util.Objects;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.database.crud.PlayerWalletCrud;
import net.stardust.base.model.channel.ChannelCondition;
import net.stardust.base.model.economy.wallet.Money;
import net.stardust.base.model.economy.wallet.PlayerWallet;

public class MoneyCondition implements ChannelCondition {

    private Money minValue;
    protected String key;

    public MoneyCondition(Money minValue) {
        this.minValue = Objects.requireNonNull(minValue, "minValue");
        key = generateKey();
    }

    protected String generateKey() {
        return "channel.no-money";
    }

    @Override
    public boolean test(CommandSender t) {
        if(!(t instanceof Player player)) return true;
        PlayerWalletCrud crud = new PlayerWalletCrud();
        PlayerWallet wallet = crud.getOrNull(player.getUniqueId());
        if(wallet == null) return false;
        return wallet.getMoney(minValue.getCurrency()).compareTo(minValue) >= 0;
    }

    @Override
    public Component getNotAllowedMessage(CommandSender sender) {
        return Component.translatable(key, NamedTextColor.RED, minValue.toComponent());
    }

}
