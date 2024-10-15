package net.stardust.base.model.channel;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.stardust.base.BasePlugin;
import net.stardust.base.Communicable;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.message.MessageFormatter;
import net.stardust.base.utils.message.Messager;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public abstract class Channel implements MessageFormatter<CommandSender>, Communicable {

    @Getter
    protected final BasePlugin plugin;

    @Getter
    protected final String name;
    protected final Set<CommandSender> participants;
    protected final List<ChannelCondition> conditions;
    protected final Messager messager;

    private boolean invokedConditions;

    public Channel(BasePlugin plugin, String name) {
        this(plugin, name, null);
    }

    public Channel(BasePlugin plugin, String name, Collection<? extends CommandSender> participants) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        messager = plugin.getMessager();
        this.name = Objects.requireNonNull(name, "name");

        Set<CommandSender> set = participants == null ? new HashSet<>() : new HashSet<>(participants);

        this.participants = Collections.synchronizedSet(set);
        this.participants.add(Bukkit.getConsoleSender());
        conditions = Collections.synchronizedList(new ArrayList<>());
    }

    public List<ChannelCondition> getConditions() {
        return new ArrayList<>();
    }

    public boolean canSendMessages(CommandSender sender) {
        if (!invokedConditions) {
            conditions.addAll(getConditions());
            invokedConditions = true;
        }
        for (ChannelCondition condition : conditions) {
            if (!condition.test(sender)) {
                messager.message(sender, condition.getNotAllowedMessage(sender));
                return false;
            }
        }
        return true;
    }

    public boolean addParticipant(CommandSender participant) {
        return participant != null && participants.add(participant);
    }

    public boolean removeParticipant(CommandSender participant) {
        return participant != null && participants.remove(participant);
    }

    public boolean containsParticipant(CommandSender participant) {
        return participant != null && participants.contains(participant);
    }

    public Set<CommandSender> getParticipants() {
        return Collections.unmodifiableSet(participants);
    }

    public void sendMessage(CommandSender sender, String message) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        sendMessage(sender, serializer.deserialize(message));
    }

    public void sendMessage(CommandSender sender, Component component) {
        if (!containsParticipant(sender))
            return;
        if (!canSendMessages(sender))
            return;
        messager.message(participants, formatMessage(sender, component));
    }

    public static Set<String> getChannels(Communicable communicable) {
        Set<String> channels = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Channel.class.getResourceAsStream("/Channels.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                channels.add(line);
            }
        } catch (IOException e) {
            Throwables.send(communicable.getId(), e);
        }
        return channels;
    }

}
