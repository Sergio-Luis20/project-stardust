package net.stardust.generalcmd;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.command.SenderType;

@BaseCommand("uid")
public class UIDCommand extends DirectCommand<GeneralCommandsPlugin> {

    public UIDCommand(GeneralCommandsPlugin plugin) {
        super(plugin);
    }

    @CommandEntry(types = SenderType.PLAYER)
    public void myUID() {
        Player sender = sender();
        sender.sendMessage(getUIDMessage("» UUID: ", sender));
    }

    @CommandEntry(opOnly = true)
    public void otherUID(String playerName) {
        CommandSender sender = sender();
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if(player == null) {
            sender.sendMessage("§c» Jogador não encontrado");
            return;
        }
        sender.sendMessage(getUIDMessage("» UUID de " + player.getName() + ": ", player));
    }

    private Component getUIDMessage(String prefix, OfflinePlayer player) {
        String uid = player.getUniqueId().toString();
        Component message = Component.text(prefix, NamedTextColor.GREEN)
            .append(Component.text(uid, NamedTextColor.AQUA)
            .hoverEvent(HoverEvent.showText(Component
            .translatable("click-to-copy", NamedTextColor.YELLOW)))
            .clickEvent(ClickEvent.copyToClipboard(uid)));
        return message;
    }
    
}
