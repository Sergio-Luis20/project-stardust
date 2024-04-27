package net.stardust.minigames;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.commands.PlaceCommand;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.command.SenderType;
import net.stardust.base.minigame.Minigame;
import net.stardust.base.minigame.Minigame.MinigameState;
import net.stardust.base.minigame.SquadChannel;
import net.stardust.base.minigame.SquadMinigame;
import org.bukkit.entity.Player;

@BaseCommand(value = "squad", types = SenderType.PLAYER)
public class SquadCommand extends DirectCommand<MinigamesPlugin> {

    public SquadCommand(MinigamesPlugin plugin) {
        super(plugin);
    }

    @CommandEntry
    public void squad(String message) {
        Player player = sender();
        Minigame match = plugin.getMatch(player);
        if(match.getState() != MinigameState.MATCH) {
            
        }
        if(match instanceof SquadMinigame squadMinigame) {
            SquadChannel channel = squadMinigame.getSquadChannel(player);

            return;
        }
        player.sendMessage(Component.translatable("minigame.no-squad", NamedTextColor.RED));
    }

}
