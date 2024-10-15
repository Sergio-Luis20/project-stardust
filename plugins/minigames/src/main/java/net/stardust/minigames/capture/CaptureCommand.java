package net.stardust.minigames.capture;

import br.sergio.utils.Pair;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.gameplay.AutomaticMessages;
import net.stardust.base.utils.world.DifferentWorldException;
import net.stardust.base.utils.world.WorldUtils;
import net.stardust.minigames.MinigamesPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
        player.sendMessage("""
                §a/capture §dcreatemap §e-> §bCria um novo mapa de capture com base no atual. \
                O que este comando faz é simplesmente criar o arquivo capture.yml na pasta do mundo.
                """);
        player.sendMessage("""
                §a/capture §dset §6<spawn|bluebase|redbase> §e-> §bDefine as localizações no \
                arquivo capture.yml do atributo passado como parâmetro do comando. A localização \
                coletada é o centro do bloco que você se encontra no momento.
                """);
        player.sendMessage("""
                §a/capture §dget §6<spawn|bluebase|redbase> §e-> §bObtém a localização do \
                atributo passado como parâmetro do comando.
                """);
        player.sendMessage("""
                §a/capture §dget §6<worldname> <spawn|bluebase|redbase> §e-> §bObtém a \
                localização do atributo passado como parâmetro do comando para o mundo especificado. \
                O nome do mundo passado possui sensibilidade a maiúsculas e minúsculas.
                """);
        player.sendMessage("""
                §a/capture §dteleport §6<spawn|bluebase|redbase> §e-> §bTeletransporta você \
                para a localização do atributo passado como parâmetro do comando.
                """);
        player.sendMessage("§a/capture §dinterrupt §e-> §bInterrompe a partida na qual você se encontra.");
    }

    @CommandEntry(value = "createmap", types = Player.class)
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
        player.sendMessage("""
                §a» Mapa de capture criado com sucesso! Não se esqueça de definir as demais \
                propriedades como spawn, base azul e base vermelha. Digite §e/capture help §a para obter \
                detalhes sobre como fazer isso.
                """);
    }

    @CommandEntry(value = "set", types = Player.class)
    public void set(CaptureLocation location) {
        Player player = sender();
        Location loc = player.getLocation().toCenterLocation().subtract(0, 0.5, 0);
        loc.setYaw(0);
        loc.setPitch(0);
        Pair<FileConfiguration, File> captureYaml = getCaptureYaml(loc.getWorld());
        if(captureYaml == null) {
            player.sendMessage("§c» Você não está num mapa de capture");
            return;
        }
        FileConfiguration config = captureYaml.getMale();
        String yamlKey = location.yamlKeyName;
        config.set(yamlKey + ".x", loc.getX());
        config.set(yamlKey + ".y", loc.getY());
        config.set(yamlKey + ".z", loc.getZ());
        try {
            config.save(captureYaml.getFemale());
        } catch(IOException e) {
            player.sendMessage(AutomaticMessages.internalServerError());
            Throwables.sendAndThrow(e);
        }
        player.sendMessage("§a» Atributo " + location.toString().toLowerCase() + " definido para §e" + loc);
    }

    @CommandEntry(value = "get", types = Player.class)
    public void get(CaptureLocation location) {
        get(((Player) sender()).getWorld().getName(), location);
    }

    @CommandEntry("get")
    public void get(String worldName, CaptureLocation location) {
        CommandSender sender = sender();
        Pair<FileConfiguration, File> captureYaml = getCaptureYaml(worldName);
        if(captureYaml == null) {
            sender.sendMessage("§c» Mundo \"" + worldName + "\" não é um mapa de capture");
            return;
        }
        ConfigurationSection section = captureYaml.getMale().getConfigurationSection(location.yamlKeyName);
        if(section == null) {
            sender.sendMessage("§c» Atributo §6" + location.toString().toLowerCase() + " §cainda não definido" +
                    "no arquivo capture.yml");
            return;
        }
        /*
         * Tries to imitate Location.toString to avoid calling Bukkit.getWorld
         * since we are getting data directly from file and the world may not be loaded.
         * This approach assumes that the world implementation is CraftWorld and the
         * implementations of toString method from both CraftWorld and Location
         * will not change in future API updates. If some of those assumptions
         * aren't correct, this code will not break, but maybe need an update.
         */
        sender.sendMessage("""
                §a» Atributo §6%s §adefinido como §eLocation{world=CraftWorld{name=%s},\
                x=%s,y=%s,z=%s,pitch=0.0,yaw=0.0}
                """.formatted(
                        worldName,
                        location.toString().toLowerCase(),
                        section.getInt("x"),
                        section.getInt("y"),
                        section.getInt("z")
                )
        );
    }

    @CommandEntry(value = "teleport", types = Player.class)
    public void teleport(CaptureLocation location) {
        Player player = sender();
        getMatch(player).ifPresentOrElse(match -> {
            Location loc = location.toLocation(match);
            player.sendMessage("§a» Teleportando você para §6" + location.toString().toLowerCase()
                    + "(" + loc + ")");
            player.teleport(loc);
        }, () -> player.sendMessage("§c» Você não está num mapa de capture"));
    }

    @CommandEntry(value = "interrupt", types = Player.class)
    public void interrupt() {
        Player player = sender();
        getMatch(player).ifPresentOrElse(match -> {
            player.sendMessage("§e» Interrompendo a partida...");
            match.interruptMatch();
        }, () -> player.sendMessage("§c» Você não está numa partida de Capture"));
    }

    private Optional<Capture> getMatch(Player player) {
        return isCaptureMap(player.getWorld()) ? plugin.getMatch(player).map(Capture.class::cast) : Optional.empty();
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

    private Pair<FileConfiguration, File> getCaptureYaml(World world) {
        return getCaptureYaml(world.getName());
    }

    private Pair<FileConfiguration, File> getCaptureYaml(String worldName) {
        File inMapsFolder = new File(getWorldMapFolder(worldName), "capture.yml");
        if(!inMapsFolder.exists()) {
            return null;
        }
        return new Pair<>(YamlConfiguration.loadConfiguration(inMapsFolder), inMapsFolder);
    }

}
