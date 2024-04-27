package net.stardust.base.model.channel.conditions;

import java.util.Map;
import java.util.Objects;

import org.bukkit.command.CommandSender;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.model.channel.ChannelCondition;

public class CooldownCondition implements ChannelCondition {

    protected final Map<CommandSender, Boolean> cooldown;
    
    @Getter
    protected final int cooldownTime;
    protected String key;

    public CooldownCondition(int cooldownTime, Map<CommandSender, Boolean> cooldown) {
        if(cooldownTime < 0) {
            throw new IllegalArgumentException("Negative cooldownTime: " + cooldownTime);
        }
        this.cooldownTime = cooldownTime;
        this.cooldown = Objects.requireNonNull(cooldown, "cooldown");
        key = generateKey();
    }

    protected String generateKey() {
        return "channel.cooldown-time";
    }

    @Override
    public boolean test(CommandSender t) {
        return !cooldown.getOrDefault(t, false);
    }

    @Override
    public Component getNotAllowedMessage(CommandSender sender) {
        return Component.translatable(key, NamedTextColor.RED, Component.text(cooldownTime, NamedTextColor.RED));
    }
    
}
