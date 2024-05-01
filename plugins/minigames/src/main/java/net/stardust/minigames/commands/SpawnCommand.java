package net.stardust.minigames.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.command.SenderType;
import net.stardust.base.minigame.Minigame;
import net.stardust.minigames.MinigamesPlugin;
import org.bukkit.entity.Player;

@BaseCommand(value = "spawn", types = SenderType.PLAYER)
public class SpawnCommand extends DirectCommand<MinigamesPlugin> {

    public SpawnCommand(MinigamesPlugin plugin) {
        super(plugin);
    }

    @CommandEntry
    public void spawn() {
        Player player = sender();
        Minigame match = plugin.getMatch(player);
        if(match == null) {
            player.sendMessage(Component.translatable("minigame.not-in-match", NamedTextColor.RED));
            return;
        }
        match.spawnCommandIssued(player);
    }

}
