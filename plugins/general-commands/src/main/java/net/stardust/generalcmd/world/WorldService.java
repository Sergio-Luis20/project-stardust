package net.stardust.generalcmd.world;

import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldService {
    
    private static final WorldService SINGLETON;

    private final List<String> worlds;

    private WorldService() {
        worlds = new ArrayList<>();
    }

    boolean createWorld(CommandSender sender, String name, List<String> options) {
        synchronized(worlds) {
            if(worlds.contains(name)) {
                sender.sendMessage("§c» Esse mundo já existe na lista de mundos criados manualmente");
                return false;
            }
            return createWorld0(sender, name, options);
        }
    }

    boolean deleteWorld(CommandSender sender, String name) {
        synchronized(worlds) {
            if(!worlds.contains(name)) {
                sender.sendMessage("§c» Esse mundo não existe na lista de mundos criados manualmente");
                return false;
            }
            return deleteWorld0(sender, name);
        }
    }

    boolean teleport(Player player, String worldName) {
        synchronized(worlds) {
            World world = Bukkit.getWorld(worldName);
            if(world == null) {
                player.sendMessage("§c» Mundo inexistente ou não carregado");
                return false;
            }
            player.sendMessage("§a» Teleportando para o mundo " + world.getName());
            return player.teleport(world.getSpawnLocation());
        }
    }

    private boolean createWorld0(CommandSender sender, String name, List<String> options) {
        WorldCreator creator = new WorldCreator(name);
        creator.generateStructures(!options.contains("false"));
        creator.environment(environment(options));
        creator.type(type(options));
        for(String option : options) {
            try {
                creator.seed(Long.parseLong(option));
            } catch(NumberFormatException e) {
                // ignored
            }
        }
        sender.sendMessage("§2» Criando mundo §b" + creator.name() + " §2com opções:");
        sender.sendMessage("§a» Gerar estruturas: §b" + creator.generateStructures());
        sender.sendMessage("§a» Ambiente: §b" + creator.environment());
        sender.sendMessage("§a» Tipo: §b" + creator.type());
        sender.sendMessage("§a» Semente: §b" + creator.seed());
        creator.createWorld();
        sender.sendMessage("§2» Mundo §b" + creator.name() + " §2criado");
        worlds.add(name);
        return true;
    }

    private boolean deleteWorld0(CommandSender sender, String name) {
        World world = Bukkit.getWorld(name);
        if(world == null) {
            File file = new File(name);
            if(file.exists()) {
                return file.delete() && worlds.remove(name);
            }
            sender.sendMessage("§c» Esse mundo não existe");
            return false;
        }
        if(name.startsWith("world") || name.startsWith("minigame") || name.startsWith("dungeon")) {
            sender.sendMessage("§c» Não é possível deletar esse mundo");
            return false;
        }
        Location spawn = Bukkit.getWorld("world").getSpawnLocation();
        sender.sendMessage("§2» Deletando mundo §b" + name);
        world.getPlayers().forEach(p -> p.teleport(spawn));
        boolean deleted = Bukkit.unloadWorld(name, false) && new File(name).delete() && worlds.remove(name);
        sender.sendMessage(deleted ? "§2» Mundo §b" + name + " §2deletado com sucesso" : "§c» Falha ao deletar o mundo §b" + name);
        return deleted;
    }

    private Environment environment(List<String> options) {
        if(options.contains("nether")) {
            return Environment.NETHER;
        } else if(options.contains("end") || options.contains("theend") || options.contains("the_end")) {
            return Environment.THE_END;
        } else if(options.contains("custom")) {
            return Environment.CUSTOM;
        } else {
            return Environment.NORMAL;
        }
    }

    private WorldType type(List<String> options) {
        if(options.contains("amplified")) {
            return WorldType.AMPLIFIED;
        } else if(options.contains("flat") || options.contains("superflat")) {
            return WorldType.FLAT;
        } else if(options.contains("largebiomes") || options.contains("large_biomes")) {
            return WorldType.LARGE_BIOMES;
        } else {
            return WorldType.NORMAL;
        }
    }

    public static WorldService get() {
        return SINGLETON;
    }

    static {
        SINGLETON = new WorldService();
    }

}
