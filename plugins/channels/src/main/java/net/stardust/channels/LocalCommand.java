package net.stardust.channels;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;

import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.model.channel.ChannelException;
import net.stardust.base.model.channel.Local;
import net.stardust.base.utils.MentionService;

@BaseCommand(value = "local", types = ConsoleCommandSender.class)
public class LocalCommand extends DirectCommand<ChannelsPlugin> {

    private MentionService service = MentionService.INSTANCE;

    public LocalCommand(ChannelsPlugin plugin) {
        super(plugin);
    }

    @CommandEntry
    public void local(String worldName, String message) {
        World world = Bukkit.getWorld(worldName);
        if(world == null) {
            sender().sendMessage("§e» Mundo inexistente ou não carregado");
            return;
        }
        Local local = LocalChat.getLocal(world);
        if(local == null) {
            throw new ChannelException("World \"" + world.getName() + "\" is loaded but has no LocalChat");
        }
        service.mention(sender(), message, local);
    }

}
