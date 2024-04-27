package net.stardust.base.model.channel.conditions;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.CommandSender;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.model.channel.ChannelCondition;

public class MuteCondition implements ChannelCondition {

    protected Map<CommandSender, Boolean> mute;
    protected String key;

    public MuteCondition() {
        mute = new ConcurrentHashMap<>();
        key = generateKey();
    }

    protected String generateKey() {
        return "channel.muted";
    }

    public boolean isMuted(CommandSender sender) {
        return mute.getOrDefault(sender, false);
    }

    public void setMuted(CommandSender sender, boolean muted) {
        mute.put(Objects.requireNonNull(sender, "sender"), muted);
    }

    @Override
    public boolean test(CommandSender t) {
        return isMuted(t);
    }

    @Override
    public Component getNotAllowedMessage(CommandSender sender) {
        return Component.translatable(key, NamedTextColor.RED);
    }
    
}
