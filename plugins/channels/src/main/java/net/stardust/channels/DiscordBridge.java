package net.stardust.channels;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import br.sergio.utils.Pair;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.stardust.base.utils.StardustThreads;
import net.stardust.base.utils.Throwables;

public class DiscordBridge extends ListenerAdapter implements Listener {

    private ChannelsPlugin plugin;
    private long botId, channelId, guildId;
    private Guild guild;
    private TextChannel channel;

    public DiscordBridge(ChannelsPlugin plugin) {
        this.plugin = plugin;
    }

    public Pair<Guild, TextChannel> setJDA(JDA jda) {
        botId = jda.getSelfUser().getIdLong();
        List<Guild> guilds = jda.getGuilds();
        int size = guilds.size();
        Logger log = plugin.getLogger();
        if(size < 1) {
            log.severe("Discord bot is not in any guild");
            throw new NotSingleGuildException("size = " + size);
        } else if(size > 1) {
            log.severe("Discord bot is in more than 1 guild");
            throw new NotSingleGuildException("size = " + size);
        } else {
            guild = guilds.getFirst();
        }
        guildId = guild.getIdLong();
        long channelId = plugin.getDiscordChannelId();
        TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
        if(channel == null) {
            throw new GuildChannelNotFoundException("Guild has no channel with id = " + channelId);
        }
        this.channelId = channelId;
        this.channel = channel;
        return new Pair<>(guild, channel);
    }

    @EventHandler
    public void transferToDiscord(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if(!plugin.getDiscordParticipants().contains(player.getUniqueId())) {
            return;
        }
        String worldTag = "[" + player.getWorld().getName() + "]";
        String name = player.getName();
        String prefix = "**" + worldTag + " " + name + ":**";
        String message = prefix + " " + PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
        MessageCreateData data = MessageCreateData.fromContent(message);
        channel.sendMessage(data).queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            if(event.getGuild().getIdLong() != guildId) {
                return;
            }
        } catch(IllegalStateException e) {
            Throwables.send(e).printStackTrace();
            return;
        }
        if(event.getChannel().getIdLong() != channelId) {
            return;
        }
        Member member = event.getMember();
        if(member.getIdLong() == botId) {
            return;
        }
        Message message = event.getMessage();
        String content = message.getContentDisplay();
        String[] urlAttachs = message.getAttachments().stream()
            .map(Attachment::getUrl).toArray(String[]::new);
        final Component finalResult = getComponent(member, content, urlAttachs);
        StardustThreads.run(plugin, () -> {
            for(UUID uid : plugin.getDiscordParticipants()) {
                Player player = Bukkit.getPlayer(uid);
                if(player != null) {
                    player.sendMessage(finalResult);
                }
            }
            Bukkit.getConsoleSender().sendMessage(finalResult);
        });
    }

    private static @NotNull Component getComponent(Member member, String content, String[] urlAttachs) {
        String nick = member.getNickname();
        Component name = Component.text((nick == null ? member.getEffectiveName() : nick) + ": ", NamedTextColor.GREEN)
            .hoverEvent(HoverEvent.showText(Component.text("Clique para adicionar menção", NamedTextColor.AQUA)))
            .clickEvent(ClickEvent.suggestCommand(member.getAsMention() + " "));
        Component result = Component.text("[Discord] ", NamedTextColor.GOLD).append(name)
            .append(Component.text(content, NamedTextColor.WHITE));
        result = !content.isEmpty() && urlAttachs.length != 0 ? result.appendSpace() : result;
        for(int i = 0; i < urlAttachs.length; i++) {
            Component attach = Component.text("Anexo-" + (i + 1), NamedTextColor.LIGHT_PURPLE)
                .hoverEvent(HoverEvent.showText(Component.text("Abrir url ", NamedTextColor.YELLOW)
                .append(Component.text(urlAttachs[i], NamedTextColor.AQUA))))
                .clickEvent(ClickEvent.openUrl(urlAttachs[i]));
            result = result.append(i == urlAttachs.length - 1 ? attach : attach
                .append(Component.text(", ", NamedTextColor.WHITE)));
        }
        return result;
    }

}
