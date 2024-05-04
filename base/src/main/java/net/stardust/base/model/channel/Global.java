package net.stardust.base.model.channel;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.BasePlugin;
import net.stardust.base.Stardust;
import net.stardust.base.model.channel.conditions.RankCondition;
import net.stardust.base.model.gameplay.Rank;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

@Getter
@ChannelProperties({"status", "minigame", "dungeon"})
public final class Global extends UpdatableCooldownMessageChannel {

    private Rank requiredRank;

    public Global(BasePlugin plugin) {
        super(plugin, "Global", 10);
    }
    
    public void load() {
        FileConfiguration config = plugin.getConfig();
        requiredRank = Rank.valueOf(config.getString("global-required-rank"));
    }

    @Override
    public Component formatMessage(CommandSender sender, Component message) {
        return Component.text()
                .append(Component.text("[G] ", NamedTextColor.GRAY))
                .append(Stardust.getIdentifier(sender).getComponentName().color(NamedTextColor.WHITE))
                .append(Component.text(": ", NamedTextColor.WHITE))
                .append(message.color(NamedTextColor.GRAY))
                .build();
    }

    @Override
    public List<ChannelCondition> getNonCooldownRelatedConditions() {
        List<ChannelCondition> conditions = new ArrayList<>();
        conditions.add(new RankCondition(requiredRank));
        return conditions;
    }

}
