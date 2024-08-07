package net.stardust.base.model.channel;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.BasePlugin;
import net.stardust.base.Stardust;
import net.stardust.base.utils.StardustThreads;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@ChannelProperties({"status", "minigame", "dungeon"})
public class Local extends SimpleCooldownChannel {

    private World world;
    private double maxDistance;

    public Local(BasePlugin plugin, World world, double maxDistance) {
        this(plugin, world, maxDistance, null);
    }

    public Local(BasePlugin plugin, World world, double maxDistance, Collection<? extends CommandSender> participants) {
        super(plugin, "Local", 1, participants);
        this.world = Objects.requireNonNull(world, "world");
        if(Double.isNaN(maxDistance)) {
            throw new IllegalArgumentException("NaN max distance for world: " + StardustThreads.call(plugin, () -> world.getName()));
        }
        if(maxDistance < 0) {
            throw new IllegalArgumentException("Negative max distance for world: " + StardustThreads.call(plugin, () -> world.getName()));
        }
        this.maxDistance = maxDistance;
    }

    @Override
    public Component formatMessage(CommandSender sender, Component message) {
        return Component.text()
                .append(Component.text("[L] ", NamedTextColor.YELLOW))
                .append(Stardust.getIdentifier(sender).getComponentName().color(NamedTextColor.WHITE))
                .append(Component.text(": ", NamedTextColor.WHITE))
                .append(message.color(NamedTextColor.YELLOW))
                .build();
    }

    @Override
    public void sendMessage(CommandSender sender, Component component) {
        if(!containsParticipant(sender)) return;
        if(!canSendMessages(sender)) return;
        Component message = formatMessage(sender, component);
        if(sender instanceof Player player) {
            if(StardustThreads.call(plugin, player::isOp)) {
                messager.message(participants, message);
                return;
            }
            AtomicBoolean fine = new AtomicBoolean();
            StardustThreads.runAndWait(plugin, () -> {
                if(world.equals(player.getWorld())) {
                    participants.forEach(participant -> {
                        if(!(participant instanceof Player other)) {
                            messager.message(participant, message);
                            return;
                        }
                        if(player.getLocation().distance(other.getLocation()) <= maxDistance) {
                            messager.message(other, message);
                        }
                    });
                    fine.set(true);
                }
            });
            if(!fine.get()) {
                throw new Error("Player is not op neither is in the world \"" 
                        + StardustThreads.call(plugin, () -> world.getName()) + "\" and is somehow participating of its local chat");
            }
            return;
        }
        messager.message(participants, message);
    }

}
