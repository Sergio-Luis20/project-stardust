package net.stardust.minigames.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.utils.gameplay.AutomaticMessages;
import net.stardust.minigames.MinigamesPlugin;

@BaseCommand(value = "minigame", types = Player.class)
public class MinigameCommand extends DirectCommand<MinigamesPlugin> {

    public MinigameCommand(MinigamesPlugin plugin) {
        super(plugin);
    }

    @CommandEntry
    public void teleport(String minigameName) {
        Player player = sender();
        Location lobby = plugin.getLobbies().get(minigameName.toLowerCase());
        if(lobby == null) {
            player.sendMessage(AutomaticMessages.notFound("word.minigame"));
        } else if(player.getWorld().equals(lobby.getWorld())) {
            player.sendMessage(Component.translatable("already-here", NamedTextColor.RED));
        } else {
            player.sendMessage(AutomaticMessages.teleportingTo(Component.text(lobby.getWorld()
                    .getName(), NamedTextColor.BLUE)));
            player.teleport(lobby);
        }
    }

}
