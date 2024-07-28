package net.stardust.base.model.channel.conditions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UpdatableCooldownCondition extends CooldownCondition {

    private Map<CommandSender, Integer> secondsLeft;

    public UpdatableCooldownCondition(int cooldownTime, Map<CommandSender, Boolean> cooldown) {
        super(cooldownTime, cooldown);
        secondsLeft = new ConcurrentHashMap<>();
    }

    @Override
    protected String generateKey() {
        return "channel.cooldown";
    }

    public int updateSecondsLeft(CommandSender sender, int seconds) {
        if(seconds < 0) throw new IllegalArgumentException("Negative seconds: " + seconds);
        secondsLeft.put(sender, seconds);
        return seconds;
    }

    @Override
    public Component getNotAllowedMessage(CommandSender sender) {
        var seconds = Component.text(secondsLeft.getOrDefault(sender, cooldownTime), NamedTextColor.DARK_RED);
        return Component.translatable(key, NamedTextColor.RED, seconds);
    }
    
}
