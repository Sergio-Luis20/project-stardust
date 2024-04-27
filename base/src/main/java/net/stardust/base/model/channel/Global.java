package net.stardust.base.model.channel;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.Stardust;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import br.sergio.utils.Pair;
import lombok.Getter;
import net.stardust.base.BasePlugin;
import net.stardust.base.model.channel.conditions.RankCondition;
import net.stardust.base.model.gameplay.Rank;
import net.stardust.base.utils.ChatUtils;
import net.stardust.base.utils.StardustThreads;

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

    private Pair<String, String> playerInfo(Player player) {
        return new Pair<String,String>(player.getWorld().getName(), player.getName());
    }

    @Override
    public List<ChannelCondition> getNonCooldownRelatedConditions() {
        List<ChannelCondition> conditions = new ArrayList<>();
        conditions.add(new RankCondition(requiredRank));
        return conditions;
    }

}
