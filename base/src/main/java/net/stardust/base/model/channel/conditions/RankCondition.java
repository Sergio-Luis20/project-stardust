package net.stardust.base.model.channel.conditions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.model.channel.ChannelCondition;
import net.stardust.base.model.gameplay.Rank;
import net.stardust.base.model.rpg.RPGPlayer;
import net.stardust.base.utils.database.crud.RPGPlayerCrud;

public class RankCondition implements ChannelCondition {

    private Rank requiredRank;
    private String key;

    public RankCondition(Rank requiredRank) {
        this.requiredRank = requiredRank == null ? Rank.E : requiredRank;
        key = generateKey();
    }

    protected String generateKey() {
        return "channel.low-rank";
    }

    @Override
    public boolean test(CommandSender t) {
        if(!(t instanceof Player player)) return true;
        RPGPlayerCrud crud = new RPGPlayerCrud();
        RPGPlayer rpgPlayer = crud.getOrNull(player.getUniqueId());
        if(rpgPlayer == null) return false;
        return Rank.COMPARATOR.compare(rpgPlayer.getRank(), requiredRank) >= 0;
    }

    @Override
    public Component getNotAllowedMessage(CommandSender sender) {
        return Component.translatable(key, NamedTextColor.RED, requiredRank.getAsComponent());
    }
    
}
