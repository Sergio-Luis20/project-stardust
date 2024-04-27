package net.stardust.base.model.channel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.command.CommandSender;

import net.stardust.base.BasePlugin;
import net.stardust.base.model.channel.conditions.CooldownCondition;
import net.stardust.base.model.channel.conditions.UpdatableCooldownCondition;
import net.stardust.base.utils.Cooldown;
import net.stardust.base.utils.Cooldown.CooldownTask;

public abstract class UpdatableCooldownMessageChannel extends CooldownChannel {

    private UpdatableCooldownCondition cooldownCondition;
    private Set<Cooldown> tasks;

    public UpdatableCooldownMessageChannel(BasePlugin plugin, String name, int cooldownTime) {
        this(plugin, name, cooldownTime, null);
    }

    public UpdatableCooldownMessageChannel(BasePlugin plugin, String name, int cooldownTime, Collection<? extends CommandSender> participants) {
        super(plugin, name, cooldownTime, participants);
        cooldownCondition = new UpdatableCooldownCondition(cooldownTime, cooldown);
        tasks = new HashSet<>();
    }

    @Override
    public CooldownCondition getCooldownCondition() {
        return cooldownCondition;
    }

    @Override
    protected void cooldownProcess(CommandSender sender) {
        cooldownCondition.updateSecondsLeft(sender, cooldownTime);
        Cooldown cd = new Cooldown(false);
        CooldownTask task = (remainingTime, unit) -> {
            if(cooldownCondition.updateSecondsLeft(sender, remainingTime) == 0) {
                tasks.remove(cd);
                cooldown.put(sender, false);
            }
            return false;
        };
        tasks.add(cd);
        cd.setTask(task);
        cd.startRepeat(cooldownTime, TimeUnit.SECONDS);
    }
    
}
