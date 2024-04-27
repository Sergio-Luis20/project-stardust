package net.stardust.base.command;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public enum SenderType {
	
	PLAYER(Player.class),
	CONSOLE(ConsoleCommandSender.class),
	COMMAND_BLOCK(BlockCommandSender.class),
	ALL(CommandSender.class);
	
	private Class<? extends CommandSender> clazz;
	
	SenderType(Class<? extends CommandSender> clazz) {
		this.clazz = clazz;
	}
	
	public Class<? extends CommandSender> getReferingClass() {
		return clazz;
	}
	
	public static SenderType getSenderType(Class<? extends CommandSender> clazz) {
		for(SenderType type : values()) {
			if(type.getReferingClass().isAssignableFrom(clazz)) {
				return type;
			}
		}
		throw new Error("Sender Type refering class not found");
	}
	
}
