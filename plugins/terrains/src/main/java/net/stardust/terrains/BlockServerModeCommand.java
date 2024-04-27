package net.stardust.terrains;

import java.util.List;

import org.bukkit.entity.Player;

import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.command.SenderType;

@BaseCommand(value = "blockservermode", types = SenderType.PLAYER)
public class BlockServerModeCommand extends DirectCommand<TerrainsPlugin> {

    public BlockServerModeCommand(TerrainsPlugin plugin) {
        super(plugin);
    }

    @CommandEntry(opOnly = true)
    public void blockServerMode() {
        List<Player> serverMode = plugin.getBlockServerMode();
        Player player = sender();
        if(serverMode.contains(player)) {
            serverMode.remove(player);
            player.sendMessage("§e» Você saiu no modo servidor de configuração de bloco");
        } else {
            serverMode.add(player);
            player.sendMessage("§e» Você entrou do modo servidor de configuração de bloco");
        }
    }
    
}
