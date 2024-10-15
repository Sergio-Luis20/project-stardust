package net.stardust.minigames.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.utils.gameplay.AutomaticMessages;
import net.stardust.minigames.MinigamesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

@BaseCommand(value = "spawn", types = Player.class)
public class SpawnCommand extends DirectCommand<MinigamesPlugin> {

    public SpawnCommand(MinigamesPlugin plugin) {
        super(plugin);
    }

    @CommandEntry
    public void spawn() {
        Player player = sender();
        plugin.getMatch(player).ifPresentOrElse(match -> match.spawnCommandIssued(player), () -> {
            World world = Bukkit.getWorld("world");
            if(world == null) {
                player.sendMessage(Component.translatable("minigame.not-in-match", NamedTextColor.RED));
            } else {
                player.sendMessage(AutomaticMessages.teleportingTo(Component.text(world.getName())));
                player.teleport(world.getSpawnLocation());
            }
        });
    }

}
