package net.stardust.base.model.channel;

import net.kyori.adventure.text.Component;
import net.stardust.base.BasePlugin;
import net.stardust.base.model.channel.conditions.CooldownCondition;
import net.stardust.base.utils.Cooldown;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class CooldownChannel extends Channel {
    
    protected final int cooldownTime;
    protected final Map<CommandSender, Boolean> cooldown;

    public CooldownChannel(BasePlugin plugin, String name, int cooldownTime) {
        this(plugin, name, cooldownTime, null);
    }

    public CooldownChannel(BasePlugin plugin, String name, int cooldownTime, Collection<? extends CommandSender> participants) {
        super(plugin, name, participants);
        if(cooldownTime < 0) {
            throw new IllegalArgumentException("Negative cooldownTime: " + cooldownTime);
        }
        this.cooldownTime = cooldownTime;
        cooldown = new ConcurrentHashMap<>();
    }

    @Override
    public final List<ChannelCondition> getConditions() {
        CooldownCondition cooldownCondition = getCooldownCondition();
        if(cooldownCondition == null) {
            throw new NullPointerException("Null cooldown condition for: " + name);
        }
        List<ChannelCondition> conditions = new ArrayList<>();
        conditions.add(cooldownCondition);
        conditions.addAll(getNonCooldownRelatedConditions());
        return conditions;
    }
    
    public void sendMessage(CommandSender sender, Component component) {
        if(!containsParticipant(sender)) return;
        if(!canSendMessages(sender)) return;
        messager.message(participants, formatMessage(sender, component));
        cooldown(sender);
    }

    public void cooldown(CommandSender sender) {
        if(!containsParticipant(sender)) return;
        if(!(sender instanceof Player)) return;
        if(cooldown.getOrDefault(sender, false)) return;
        cooldown.put(sender, true);
        cooldownProcess(sender);
    }
    
    protected void cooldownProcess(CommandSender sender) {
        Cooldown cd = new Cooldown(false);
        cd.setTask((remainingTime, unit) -> cooldown.put(sender, false));
        cd.start(cooldownTime, TimeUnit.SECONDS);
    }

    public abstract CooldownCondition getCooldownCondition();

    public List<ChannelCondition> getNonCooldownRelatedConditions() {
        return new ArrayList<>();
    }

}
