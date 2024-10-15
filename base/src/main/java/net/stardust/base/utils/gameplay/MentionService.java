package net.stardust.base.utils.gameplay;

import br.sergio.utils.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.stardust.base.model.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class MentionService {

    public static final MentionService INSTANCE = new MentionService();
    public static final SoundPack MENTION_SOUND = new SoundPack(Sound.BLOCK_NOTE_BLOCK_HARP);
    public static final Pattern MENTION_PATTERN = Pattern.compile("@\\w{3,16}");

    public Pair<Component, Set<Player>> processMessage(String str) {
        String[] message = str.split(" ");
        Set<Player> players = new HashSet<>();
        for(int i = 0; i < message.length; i++) {
            if(MENTION_PATTERN.matcher(message[i]).matches()) {
                String playerName = message[i].substring(1);
                Player player = Bukkit.getPlayer(playerName);
                if(player != null) {
                    message[i] = "§9@" + player.getName() + "§r";
                    players.add(player);
                }
            }
        }
        return new Pair<>(LegacyComponentSerializer.legacySection()
            .deserialize(String.join(" ", message)), players);
    }
    
    public Pair<Component, Set<Player>> processMessage(Component component) {
        return processMessage(LegacyComponentSerializer.legacySection().serialize(component));
    }

    public void mentionSound(Set<Player> players) {
        players.forEach(this::mentionSound);
    }

    public void mentionSound(Player player) {
        MENTION_SOUND.play(player);
    }

    public void mention(CommandSender sender, String message, Channel channel) {
        Pair<Component, Set<Player>> mention = processMessage(message);
        channel.sendMessage(sender, mention.getMale());
        mention.getFemale().forEach(p -> {
            if(channel.containsParticipant(p)) mentionSound(p);
        });
    }

    public void mention(CommandSender sender, Component message, Channel channel) {
        mention(sender, LegacyComponentSerializer.legacySection().serialize(message), channel);
    }

}
