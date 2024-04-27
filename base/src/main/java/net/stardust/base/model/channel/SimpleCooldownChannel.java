package net.stardust.base.model.channel;

import java.util.Collection;

import org.bukkit.command.CommandSender;

import net.stardust.base.BasePlugin;
import net.stardust.base.model.channel.conditions.CooldownCondition;

public abstract class SimpleCooldownChannel extends CooldownChannel {

    public SimpleCooldownChannel(BasePlugin plugin, String name, int cooldownTime) {
        super(plugin, name, cooldownTime);
    }

    public SimpleCooldownChannel(BasePlugin plugin, String name, int cooldownTime, Collection<? extends CommandSender> participants) {
        super(plugin, name, cooldownTime, participants);
    }

    @Override
    public CooldownCondition getCooldownCondition() {
        return new CooldownCondition(cooldownTime, cooldown);
    }

}
