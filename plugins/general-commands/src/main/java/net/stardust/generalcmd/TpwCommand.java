package net.stardust.generalcmd;

import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.command.SenderType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

@BaseCommand(value = "tpw", types = SenderType.PLAYER)
public class TpwCommand extends DirectCommand<GeneralCommandsPlugin> {

    public TpwCommand(GeneralCommandsPlugin plugin) {
        super(plugin);
    }
    
    @CommandEntry(opOnly = true)
    public void tpw(String worldName) {
        Player player = sender();
        World world = Bukkit.getWorld(worldName);
        if(world == null) {
            player.sendMessage("§c» Mundo inexistente");
            return;
        }
        player.sendMessage("§a» Teleportando para o mundo §9" + worldName);
        player.teleport(world.getSpawnLocation());
    }

}
