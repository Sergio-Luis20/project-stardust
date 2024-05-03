package net.stardust.generalcmd;

import br.sergio.utils.FileManager;
import net.stardust.base.Stardust;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.utils.AutomaticMessages;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.world.WorldUtils;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@BaseCommand(value = "world", opOnly = true)
public class WorldCommand extends DirectCommand<GeneralCommandsPlugin> {

    private List<World> worlds = new ArrayList<>();

    public WorldCommand(GeneralCommandsPlugin plugin) {
        super(plugin);
    }

    @CommandEntry(value = "create", oneWordFinalString = true)
    public void create(String name) {
        create(name, Collections.emptyList());
    }

    @CommandEntry("create")
    public void create(String name, String options) {
        create(name, Arrays.stream(options.split(" ")).map(String::toLowerCase).toList());
    }

    @CommandEntry(value = "delete", oneWordFinalString = true)
    public void delete(String name) {
        CommandSender sender = sender();
        World world = Bukkit.getWorld(name);
        if(world == null) {
            File file = new File(name);
            if(file.exists()) {
                deleteDir(sender, file.toPath());
                sender.sendMessage("§2» Mundo §b" + name + " §2deletado com sucesso");
                return;
            }
            sender.sendMessage("§c» Esse mundo não existe");
        } else if(name.startsWith("world") || name.startsWith("minigame") || name.startsWith("dungeon")) {
            sender.sendMessage("§c» Não é possível deletar esse mundo");
        } else if(!worlds.contains(world)) {
            sender.sendMessage("§c» Exxe mundo não existe na lista de mundos criados manualmente");
        } else {
            Location spawn = Bukkit.getWorld("world").getSpawnLocation();
            sender.sendMessage("§2» Deletando mundo §b" + name);
            world.getPlayers().forEach(p -> p.teleport(spawn));
            boolean deleted = worlds.remove(world) && WorldUtils.unloadWorld(world, false);
            sender.sendMessage(deleted ? "§2» Mundo §b" + name + " §2deletado com sucesso" : "§c» Falha ao deletar o mundo §b" + name);
        }
    }

    @CommandEntry("list")
    public void list() {
        list(1);
    }

    @CommandEntry("list")
    public void list(int page) {
        Stardust.listPageableString(sender(), page, worlds, "worlds-manually-loaded", World::getName);
    }

    private void create(String worldName, List<String> options) {
        CommandSender sender = sender();
        if(Bukkit.getWorld(worldName) != null) {
            sender.sendMessage("§c» O mundo §e" + worldName + " §cjá existe");
        } else if(worldName.startsWith("world") || worldName.startsWith("terrain")
                || worldName.startsWith("minigame") || worldName.startsWith("dungeon")) {
            sender.sendMessage("§c» Não é possível criar um mundo com esse nome");
        } else {
            WorldCreator creator = new WorldCreator(worldName);
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

            World world = creator.createWorld();
            worlds.add(world);

            sender.sendMessage("§2» Mundo §b" + creator.name() + " §2criado");
        }
    }

    private void deleteDir(CommandSender sender, Path dir) {
        try {
            FileManager.delete(dir);
        } catch(IOException e) {
            sender.sendMessage(AutomaticMessages.internalServerError());
            Throwables.sendAndThrow(e);
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

}
