package net.stardust.minigames.capture;

import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.command.SenderType;
import net.stardust.base.utils.MultiWriterYamlConfiguration;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.world.DifferentWorldException;
import net.stardust.base.utils.world.WorldUtils;
import net.stardust.minigames.MinigamesPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@BaseCommand(value = "capture", opOnly = true)
public class CaptureCommand extends DirectCommand<MinigamesPlugin> {

    public CaptureCommand(MinigamesPlugin plugin) {
        super(plugin);
    }

    @CommandEntry("help")
    public void help() {
        Player player = sender();
        player.sendMessage("§d» Lista de opções para o comando §c/capture§d:");
        player.sendMessage("§a/capture §dcreatemap §e-> §bCria um novo mapa de capture com base no atual." +
                " O que este comando faz é simplesmente criar o arquivo capture.yml na pasta do mundo.");
        player.sendMessage("§a/capture §dset §6<spawn|bluebase|redbase> §e-> §bDefine as localizações no" +
                " arquivo capture.yml do atributo passado como parâmetro do comando. A localização coletada" +
                " é o centro do bloco que você se encontra no momento.");
        player.sendMessage("§a/capture §dget §6<spawn|bluebase|redbase> §e-> §bObtém a localização do " +
                "atributo passado como parâmetro do comando.");
        player.sendMessage("§a/capture §dget §6<worldname> <spawn|bluebase|redbase> §e-> §bObtém a " +
                "localização do atributo passado como parâmetro do comando para o mundo especificado. " +
                "O nome do mundo passado possui sensibilidade a maiúsculas e minúsculas");
        player.sendMessage("§a/capture §dteleport §6<spawn|bluebase|redbase> §e-> §bTeletransporta você " +
                "para a localização do atributo passado como parâmetro do comando.");
        player.sendMessage("§a/capture §dinterrupt §e-> §bInterrompe a partida na qual você se encontra.");
    }

    @CommandEntry(value = "createmap", types = SenderType.PLAYER)
    public void createMap() {
        Player player = sender();
        World world = player.getWorld();
        UUID worldUID = world.getUID();
        File mapFolder = getWorldMapFolder(world);
        UUID otherUID = WorldUtils.readWorldUUID(mapFolder);
        if(!worldUID.equals(otherUID)) {
            DifferentWorldException e = new DifferentWorldException("Folder " + mapFolder.getPath()
                    + " (UID: " + otherUID + ") is not the same of the world " + world.getName()
                    + " (UID: " + worldUID + ")");
            player.sendMessage("§c» Erro durante a verificação: " + e);
            throw Throwables.send(e);
        }
        File configFile = new File(mapFolder, "capture.yml");
        try {
            if(!configFile.createNewFile()) {
                player.sendMessage("§e» O mundo atual já é um mapa de capture");
            }
        } catch(IOException e) {
            player.sendMessage("§c» Erro durante a criação do arquivo de configuração na pasta do mapa (" + e + ")");
            Throwables.sendAndThrow(e);
        }
        player.sendMessage("§a» Mapa de capture criado com sucesso! Não se esqueça de definir as demais " +
                "propriedades como spawn, base azul e base vermelha. Digite §e/capture help §a para obter " +
                "detalhes sobre como fazer isso.");
    }

    @CommandEntry(value = "set", types = SenderType.PLAYER)
    public void set(CaptureLocation location) {
        Player player = sender();
        Location loc = player.getLocation().toCenterLocation().subtract(0, 0.5, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        MultiWriterYamlConfiguration captureYaml = getCaptureYaml(loc.getWorld());
        if(captureYaml == null) {
            player.sendMessage("§c» Você não está num mapa de capture");
            return;
        }
        String yamlKey = location.yamlKeyName;
        captureYaml.set(yamlKey + ".x", loc.getX());
        captureYaml.set(yamlKey + ".y", loc.getY());
        captureYaml.set(yamlKey + ".z", loc.getZ());
        captureYaml.save();
        player.sendMessage("§a» Atributo " + location.toString().toLowerCase() + " definido para §e" + loc);
    }

    @CommandEntry(value = "get", types = SenderType.PLAYER)
    public void get(CaptureLocation location) {
        get(((Player) sender()).getWorld().getName(), location);
    }

    @CommandEntry("get")
    public void get(String worldName, CaptureLocation location) {
        CommandSender sender = sender();
        MultiWriterYamlConfiguration captureYaml = getCaptureYaml(worldName);
        if(captureYaml == null) {
            sender.sendMessage("§c» Mundo \"" + worldName + "\" não é um mapa de capture");
            return;
        }
        ConfigurationSection section = captureYaml.getConfigurationSection(location.yamlKeyName);
        if(section == null) {
            sender.sendMessage("§c» Atributo §6" + location.toString().toLowerCase() + " §cainda não definido" +
                    "no arquivo capture.yml");
            return;
        }
        /*
         * Tries to imitate Location.toString to avoind calling Bukkit.getWorld
         * since we are getting data directly from file and the world may not be loaded.
         * This approach assumes that the world implementation is CraftWorld and the
         * implementations of toString method from both CraftWorld and Location
         * will not change in future API updates. If some of those assumptions
         * aren't correct, this code will not break, but maybe need an update.
         */
        String worldToString = "CraftWorld{name=" + worldName + "}";
        sender.sendMessage("§a» Atributo §6" + location.toString().toLowerCase() + " §adefinido como: §e" +
                "Location{world=" + worldToString + ",x=" + section.getInt("x") + ",y=" +
                section.getInt("y") + ",z=" + section.getInt("z") + ",pitch=0.0,yaw=0.0}");
    }

    @CommandEntry(value = "teleport", types = SenderType.PLAYER)
    public void teleport(CaptureLocation location) {
        Player player = sender();
        getMatch(player).ifPresentOrElse(match -> {
            Location loc = location.toLocation(match);
            player.sendMessage("§a» Teleportando você para §6" + location.toString().toLowerCase()
                    + "(" + loc + ")");
            player.teleport(loc);
        }, () -> player.sendMessage("§c» Você não está num mapa de capture"));
    }

    @CommandEntry(value = "interrupt", types = SenderType.PLAYER)
    public void interrupt() {
        Player player = sender();
        getMatch(player).ifPresentOrElse(match -> {
            player.sendMessage("§e» Interrompendo a partida...");
            match.interruptMatch();
        }, () -> player.sendMessage("§c» Você não está numa partida de Capture"));
    }

    private Optional<Capture> getMatch(Player player) {
        return isCaptureMap(player.getWorld()) ? Optional.of((Capture)
                plugin.getMatch(player)) : Optional.empty();
    }

    private boolean isCaptureMap(World world) {
        return new File(getWorldMapFolder(world), "capture.yml").exists();
    }

    private File getWorldMapFolder(World world) {
        return getWorldMapFolder(world.getName());
    }

    private File getWorldMapFolder(String worldName) {
        return new File(plugin.getMapsFolder(), "capture/" + worldName);
    }

    private MultiWriterYamlConfiguration getCaptureYaml(World world) {
        return getCaptureYaml(world.getName());
    }

    private MultiWriterYamlConfiguration getCaptureYaml(String worldName) {
        File inMapsFolder = new File(getWorldMapFolder(worldName), "capture.yml");
        if(!inMapsFolder.exists()) {
            return null;
        }
        File inServerFolder = new File(worldName);
        MultiWriterYamlConfiguration config = new MultiWriterYamlConfiguration(inServerFolder);
        if(inServerFolder.exists()) {
            config.getFiles().add(inServerFolder);
        }
        return config;
    }

}