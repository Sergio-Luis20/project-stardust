package net.stardust.minigames.capture;

import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.stardust.base.Stardust;
import net.stardust.base.minigame.Minigame;
import net.stardust.base.minigame.SquadChannel;
import net.stardust.base.minigame.SquadMinigame;
import net.stardust.base.model.Identifier;
import net.stardust.base.utils.database.lang.Translation;
import net.stardust.minigames.MinigamesPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Capture extends Minigame implements SquadMinigame {

    private MinigamesPlugin plugin;

    @Getter(AccessLevel.PACKAGE)
    private List<Player> blueTeam, redTeam;
    private Location blueBase, redBase, spawn;
    private SquadChannel blueChannel, redChannel;

    public Capture(MinigamesPlugin plugin) {
        super(plugin.getMinigameInfo("capture"));
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    protected void match() {
        distributePlayers();
        setupBasesAndSpawn();
        setupSquadChannels();
    }

    private void distributePlayers() {
        World world = getWorld();
        List<Player> players = world.getPlayers();
        Collections.shuffle(players);
        CaptureTeam team = CaptureTeam.randomTeam();
        for(Player player : players) {
            team.getPlayers(this).add(player);
            team = team.other();
        }
    }

    private void setupBasesAndSpawn() {
        File mapConfigurationFile = new File(getWorld().getWorldFolder(), "capture.yml");
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(mapConfigurationFile);
        blueBase = getLocation(configuration.getConfigurationSection("blue-base"));
        redBase = getLocation(configuration.getConfigurationSection("red-base"));
        spawn = getLocation(configuration.getConfigurationSection("spawn"));
    }

    private void setupSquadChannels() {
        blueChannel = new SquadChannel(plugin, "Capture Blue Squad Channel", blueTeam,
                (sender, message) -> formatTeamMessage(sender, message, NamedTextColor.BLUE, "blue"));
        redChannel = new SquadChannel(plugin, "Capture Red Squad Channel", redTeam,
                (sender, message) -> formatTeamMessage(sender, message, NamedTextColor.RED, "red"));
    }

    private Component formatTeamMessage(CommandSender sender, Component message, TextColor color, String colorKey) {
        Component name = Stardust.getIdentifier(sender).getComponentName().color(color);
        Component colorName = Translation.getTextComponent(sender, "color." + colorKey).color(color);
        Component tagComponent = Component.text("[", color).append(colorName)
                .append(Component.text("]", color));
        return tagComponent.appendSpace().append(name).append(Component.text(": ", color))
                .append(message.color(color));
    }

    private Location getLocation(ConfigurationSection section) {
        return new Location(getWorld(), section.getDouble("x"), section.getDouble("y"), section.getDouble("x"));
    }

    @Override
    public List<SquadChannel> getSquadChannels() {
        return List.of(blueChannel, redChannel);
    }

    @Override
    public SquadChannel getSquadChannel(Player player) {
        if(blueChannel.containsParticipant(player)) {
            return blueChannel;
        } else if(redChannel.containsParticipant(player)) {
            return redChannel;
        } else {
            return null;
        }
    }

    public Location getBlueBase() {
        return blueBase.clone();
    }

    public Location getRedBase() {
        return redBase.clone();
    }

    public Location getSpawn() {
        return spawn.clone();
    }

}
