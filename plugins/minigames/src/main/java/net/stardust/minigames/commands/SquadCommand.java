package net.stardust.minigames.commands;

import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.minigame.Minigame;
import net.stardust.base.minigame.Minigame.MinigameState;
import net.stardust.base.minigame.SquadChannel;
import net.stardust.base.minigame.SquadMinigame;
import net.stardust.base.utils.AutomaticMessages;
import net.stardust.base.utils.Throwables;
import net.stardust.minigames.MinigamesPlugin;

@BaseCommand(value = "squad", types = Player.class, usageKey = "minigame.team.usage")
public class SquadCommand extends DirectCommand<MinigamesPlugin> {

    public SquadCommand(MinigamesPlugin plugin) {
        super(plugin);
    }

    @CommandEntry
    public void squad(String message) {
        Player player = sender();
        Minigame match = plugin.getMatch(player);
        if(match == null) {
            player.sendMessage(Component.translatable("minigame.not-in-match", NamedTextColor.RED));
            return;
        }
        if(match.getState() != MinigameState.MATCH) {
            player.sendMessage(Component.translatable("minigame.not-in-match-state", NamedTextColor.RED));
            return;
        }
        if(match instanceof SquadMinigame squadMinigame) {
            SquadChannel channel = squadMinigame.getSquadChannel(player);
            if(channel == null) {
                IllegalStateException e = new IllegalStateException("Null squad channel while issuing " +
                        "squad command at running match. Minigame name: " + match.getInfo().name());
                player.sendMessage(AutomaticMessages.internalServerError());
                Throwables.sendAndThrow(e);
            }
            channel.sendMessage(player, message);
        } else {
            player.sendMessage(Component.translatable("minigame.no-squad", NamedTextColor.RED));
        }
    }

}
