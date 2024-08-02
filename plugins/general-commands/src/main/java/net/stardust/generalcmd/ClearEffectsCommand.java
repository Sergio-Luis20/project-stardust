package net.stardust.generalcmd;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;

@BaseCommand(value = "clearpotions", opOnly = true)
public class ClearEffectsCommand extends DirectCommand<GeneralCommandsPlugin> {

    public ClearEffectsCommand(GeneralCommandsPlugin plugin) {
        super(plugin);
    }

    @CommandEntry(types = Player.class)
    public void clear() {
        clear((Player) sender());
    }

    @CommandEntry
    public void clear(String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if(target == null) {
            sender().sendMessage("§c» Jogador não encontrado");
            return;
        }
        clear(target);
    }

    private void clear(Player target) {
        target.clearActivePotionEffects();
        sender().sendMessage("§a» Efeitos de poções de §b" + target.getName() + " §alimpos");
    }

}
