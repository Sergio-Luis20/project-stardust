package net.stardust.base.minigame;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.stardust.base.BasePlugin;
import net.stardust.base.model.channel.Channel;
import net.stardust.base.utils.message.MessageFormatter;

import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Objects;

@Getter
public class SquadChannel extends Channel {

    private MessageFormatter<CommandSender> formatter;

    public SquadChannel(BasePlugin plugin, String name, Collection<? extends CommandSender> participants, MessageFormatter<CommandSender> formatter) {
        super(plugin, name, participants);
        this.formatter = Objects.requireNonNull(formatter, "formatter");
    }

    @Override
    public Component formatMessage(CommandSender sender, Component message) {
        return formatter.formatMessage(sender, message);
    }

}
