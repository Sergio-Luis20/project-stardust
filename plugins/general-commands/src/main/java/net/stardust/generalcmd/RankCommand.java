package net.stardust.generalcmd;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.VirtualCommand;
import net.stardust.base.database.crud.RPGPlayerCrud;
import net.stardust.base.database.crud.UserCrud;
import net.stardust.base.database.lang.Translation;
import net.stardust.base.model.gameplay.Rank;
import net.stardust.base.model.rpg.RPGPlayer;
import net.stardust.base.model.user.User;

@BaseCommand("rank")
public class RankCommand extends VirtualCommand<GeneralCommandsPlugin> {
	
	private UserCrud userCrud;
	private RPGPlayerCrud rpgPlayerCrud;

    public RankCommand(GeneralCommandsPlugin plugin) {
        super(plugin);
        userCrud = new UserCrud();
        rpgPlayerCrud = new RPGPlayerCrud();
    }
    
    @CommandEntry(types = Player.class)
    public void getSubCommand() {
    	Player sender = sender();
    	executeGet(sender, name(sender), rpgPlayerCrud.getOrThrow(uniqueId(sender)));
    }
    
    @CommandEntry
    public void getSubCommand(String name) {
    	getByName(name);
    }
    
    @CommandEntry("help")
    public void help() {
    	CommandSender sender = sender();
		int range = isOp(sender) ? 5 : 4;
		Component[] messages = new Component[range];
        for(int i = 0; i < range; i++) {
        	messages[i] = Translation.get(sender, "rank.help.m" + i);
        }
        messager.message(sender, messages);
    }
    
    @CommandEntry("get")
    public void get0(String name) {
    	getByName(name);
    }
    
    @CommandEntry(value = "set", opOnly = true)
    public void set(String name, Rank rank) {
    	CommandSender sender = sender();
    	User target = userCrud.byNameOrNull(name);
    	if(target == null) {
    		messager.message(sender, "§cJogador não encontrado");
    		return;
    	}
    	RPGPlayer player = rpgPlayerCrud.getOrThrow(target.getId());
    	Rank oldRank = player.getRank();
    	if(oldRank == rank) {
    		messager.message(sender, "§eNada mudou");
    		return;
    	}
    	player.setRank(rank);
    	if(rpgPlayerCrud.update(player)) {
    		messager.message(sender, "§b» Rank de §d" + target.getName() + " §batualizado de " + oldRank.getColor() 
    				+ oldRank + " §bpara " + rank.getColor() + rank);
    		return;
    	}
    	messager.message(sender, "§cNão foi possível atualizar o rank de §4" + target.getName());
    }
    
    private void getByName(String name) {
    	CommandSender sender = sender();
    	User target = userCrud.byNameOrNull(name);
    	if(target == null) {
    		messager.message(sender, Component.translatable("not-found", NamedTextColor.RED, 
				Component.translatable("word.player", NamedTextColor.RED)));
    		return;
    	}
    	executeGet(sender, target.getName(), rpgPlayerCrud.getOrThrow(target.getId()));
    }
    
    private void executeGet(CommandSender sender, String name, RPGPlayer player) {
    	Rank rank = player.getRank();
    	messager.message(sender, Component.translatable("rank.message", NamedTextColor.AQUA, 
			Component.text(name, NamedTextColor.GREEN), Component.text(rank.toString(), rank.getTextColor())));
    }
    
}
