package net.stardust.channels;

import net.dv8tion.jda.api.OnlineStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.stardust.base.Stardust;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.SenderType;
import net.stardust.base.command.VirtualCommand;
import net.stardust.base.utils.StardustThreads;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@BaseCommand(value = "discordchat", types = SenderType.PLAYER)
public class DiscordChatCommand extends VirtualCommand<ChannelsPlugin> {
    
    public DiscordChatCommand(ChannelsPlugin plugin) {
        super(plugin);
    }

    @CommandEntry(value = "enter", opOnly = true)
    public void enter() {
        Player player = sender();
        UUID uid = uniqueId(player);
        Set<UUID> discordParticipants = plugin.getDiscordParticipants();
        if(discordParticipants.contains(uid)) {
            messager.message(player, "§c» Você já está no chat do discord");
            return;
        }
        discordParticipants.add(uid);
        messager.message(player, "§a» Você agora está no chat do discord");
    }

    @CommandEntry(value = "add", opOnly = true)
    public void add(String playerName) {
        Player sender = sender();
        OfflinePlayer offlinePlayer = StardustThreads.call(plugin, () -> Bukkit.getOfflinePlayer(playerName));
        if(offlinePlayer == null) {
            messager.message(sender, "§c» Jogador inexistente");
            return;
        }
        UUID uid = StardustThreads.call(plugin, offlinePlayer::getUniqueId);
        Set<UUID> discordParticipants = plugin.getDiscordParticipants();
        if(discordParticipants.contains(uid)) {
            messager.message(sender, "§c» O jogador alvo já está no chat do discord");
            return;
        }
        discordParticipants.add(uid);
        messager.message(sender, "§a» §b" + StardustThreads
            .call(plugin, offlinePlayer::getName) + " §aestá agora no chat do discord");
        if(offlinePlayer instanceof Player player) {
            messager.message(player, "§a» §b" + name(sender) + " §acolocou você no chat do discord");
        }
    }

    @CommandEntry(value = "remove", opOnly = true)
    public void remove(String playerName) {
        Player sender = sender();
        OfflinePlayer offlinePlayer = StardustThreads.call(plugin, () -> Bukkit.getOfflinePlayer(playerName));
        if(offlinePlayer == null) {
            messager.message(sender, "§c» Jogador inexistente");
            return;
        }
        UUID uid = StardustThreads.call(plugin, offlinePlayer::getUniqueId);
        Set<UUID> discordParticipants = plugin.getDiscordParticipants();
        if(!discordParticipants.contains(uid)) {
            messager.message(sender, "§c» O jogador alvo já não está no chat do discord");
            return;
        }
        discordParticipants.remove(uid);
        messager.message(sender, "§a» §b" + StardustThreads
            .call(plugin, offlinePlayer::getName) + " §afoi removido do chat do discord");
        if(offlinePlayer instanceof Player player) {
            messager.message(player, "§c» §b" + name(sender) + " §aremoveu você do chat do discord");
        }
    }

    @CommandEntry("leave")
    public void leave() {
        Player player = sender();
        UUID uid = uniqueId(player);
        Set<UUID> discordParticipants = plugin.getDiscordParticipants();
        if(discordParticipants.contains(uid)) {
            messager.message(player, "§c» Você já não estava no chat do discord");
            return;
        }
        discordParticipants.remove(uid);
        messager.message(player, "§a» Você saiu do chat do discord");
    }

    @CommandEntry(value = "has", opOnly = true)
    public void has(String playerName) {
        Player sender = sender();
        OfflinePlayer offlinePlayer = StardustThreads.call(plugin, () -> Bukkit.getOfflinePlayer(playerName));
        if(offlinePlayer == null) {
            messager.message(sender, "§c» Jogador inexistente");
            return;
        }
        UUID uid = StardustThreads.call(plugin, offlinePlayer::getUniqueId);
        Set<UUID> discordParticipants = plugin.getDiscordParticipants();
        messager.message(sender, "§a» O jogador alvo " + (discordParticipants
            .contains(uid) ? "" : "§cnão §a") + "está no chat do discord");
    }

    @CommandEntry("ami")
    public void ami() {
        Player sender = sender();
        UUID uid = uniqueId(sender);
        Set<UUID> discordParticipants = plugin.getDiscordParticipants();
        messager.message(sender, "§a» Você " + (discordParticipants
            .contains(uid) ? "" : "§cnão §a") + "está no chat do discord");
    }

    @CommandEntry(value = "list", opOnly = true)
    public void listPage1() {
        list(1);
    }

    @CommandEntry(value = "list", opOnly = true)
    public void listPageN(int page) {
        list(page);
    }

    @CommandEntry(value = "users", opOnly = true)
    public void usersPage1() {
        usersPage1Online(true);
    }

    @CommandEntry(value = "users", opOnly = true)
    public void usersPage1Online(boolean online) {
        users(online, 1);
    }

    @CommandEntry(value = "users", opOnly = true)
    public void usersPageN(int page) {
        usersPageNOnline(true, page);
    }

    @CommandEntry(value = "users", opOnly = true)
    public void usersPageNOnline(boolean online, int page) {
        users(online, page);
    }

    private void list(int page) {
        Player player = sender();
        String key = "discord-chat";
        Stream<String> names = StardustThreads.call(plugin, () -> plugin
                .getDiscordParticipants().stream().map(Bukkit::getOfflinePlayer)
                .filter(op -> op != null).map(OfflinePlayer::getName));
        Stardust.listPageableString(player, page, names, key, name -> name);
    }

    private void users(boolean online, int page) {
        Player player = sender();
        String key = "discord-users." + (online ? "online-only" : "offline-too");
        HoverEvent<Component> hover = HoverEvent.showText(Component.text("Clique para copiar", NamedTextColor.AQUA));
        Component mention = Component.text("Menção", NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC)
            .hoverEvent(HoverEvent.showText(Component.text("Clique para mencionar", NamedTextColor.AQUA)));
        Component nameComp = Component.text("Name: ", NamedTextColor.GREEN);
        Component nickComp = Component.text("Nick: ", NamedTextColor.GREEN);
        Component idComp = Component.text("Id: ", NamedTextColor.GREEN);
        Component comma = Component.text(", ", NamedTextColor.WHITE);
        List<OnlineStatus> status = online ? Arrays.asList(OnlineStatus.ONLINE, 
            OnlineStatus.IDLE, OnlineStatus.DO_NOT_DISTURB) : Arrays.asList(OnlineStatus.values());
        Stream<Component> elements = plugin.getJDAInfo().getMale().getMembers().stream()
                .filter(member -> status.contains(member.getOnlineStatus())).map(member -> {
                    String name = member.getEffectiveName();
                    String nick = member.getNickname();
                    String id = member.getId();
                    Component namePiece = nameComp.append(Component.text(name, NamedTextColor.AQUA))
                            .hoverEvent(hover).clickEvent(ClickEvent.copyToClipboard(name));
                    Component nickPiece = nickComp.append(Component.text(String.valueOf(nick == null ? null : "\"" + nick + "\"")))
                            .hoverEvent(hover).clickEvent(ClickEvent.copyToClipboard(nick == null ? "null" : nick));
                    Component idPiece = idComp.append(Component.text(id, NamedTextColor.AQUA)).hoverEvent(hover)
                            .clickEvent(ClickEvent.copyToClipboard(id));
                    Component mentionPiece = mention.clickEvent(ClickEvent.suggestCommand(member.getAsMention() + " "));
                    return namePiece.append(comma).append(nickPiece).append(comma).append(idPiece)
                            .append(comma).append(mentionPiece);
                });
        Stardust.listPageable(player, page, elements, key, e -> e);
    }

}
