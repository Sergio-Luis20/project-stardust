package net.stardust.base.model.channel;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.BasePlugin;
import net.stardust.base.Stardust;
import net.stardust.base.model.channel.conditions.MoneyCondition;
import net.stardust.base.model.channel.conditions.RankCondition;
import net.stardust.base.model.economy.wallet.Money;
import net.stardust.base.model.gameplay.Rank;

@Getter
@ChannelProperties({"status", "minigame", "dungeon"})
public final class Ad extends SimpleCooldownChannel {

    private Rank requiredRank;
    private Money usagePrice, deactivationPrice;

    public Ad(BasePlugin plugin) {
        super(plugin, "Ad", 60);
    }
    
    public void load() {
        FileConfiguration config = plugin.getConfig();
        requiredRank = Rank.valueOf(config.getString("ad-required-rank"));
        usagePrice = Money.valueOf(config.getString("ad-usage-price"));
        deactivationPrice = Money.valueOf(config.getString("ad-deactivation-price"));
    }

    @Override
    public Component formatMessage(CommandSender sender, Component message) {
        return Component.text()
                .append(Component.text("[A] ", NamedTextColor.GREEN))
                .append(Stardust.getIdentifier(sender).getComponentName().color(NamedTextColor.WHITE))
                .append(Component.text(": ", NamedTextColor.WHITE))
                .append(message.color(NamedTextColor.GREEN))
                .build();
    }

    @Override
    public List<ChannelCondition> getNonCooldownRelatedConditions() {
        List<ChannelCondition> conditions = new ArrayList<>();
        conditions.add(new RankCondition(requiredRank));
        conditions.add(new MoneyCondition(usagePrice));
        return conditions;
    }

}
