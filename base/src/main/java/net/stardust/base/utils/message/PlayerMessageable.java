package net.stardust.base.utils.message;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.stardust.base.model.user.PlayerIdentifier;

/**
 * {@link Messageable} implementation class that sends messages
 * to a {@link Player}.
 * 
 * @see Messageable
 * @see Player
 * 
 * @author Sergio Luis
 */
public class PlayerMessageable extends PlayerIdentifier implements Messageable {

    /**
     * Constructs a new {@link PlayerMessageable} instance with
     * the provided player id.
     * 
     * @see Messageable
     * @see PlayerIdentifier
     * @see PlayerIdentifier#PlayerIdentifier(UUID)
     * @param id the player id
     * @throws NullPointerException if id is null
     */
    public PlayerMessageable(UUID id) {
        super(id);
    }

    /**
     * Constructs a new {@link PlayerMessageable} instance with
     * the provided player.
     * 
     * @see Messageable
     * @see PlayerIdentifier
     * @see PlayerIdentifier#PlayerIdentifier(Player)
     * @param player the player
     * @throws NullPointerException if player is null
     */
    public PlayerMessageable(Player player) {
        super(player);
    }

    /**
     * Constructs a new {@link PlayerMessageable} instance with
     * the provided offline player. Check {@link PlayerIdentifier#PlayerIdentifier(OfflinePlayer)}
     * documentation for important details.
     * 
     * @see Messageable
     * @see PlayerIdentifier
     * @see PlayerIdentifier#PlayerIdentifier(OfflinePlayer)
     * @param offlinePlayer
     * @throws NullPointerException if offline player is null
     */
    public PlayerMessageable(OfflinePlayer offlinePlayer) {
        super(offlinePlayer);
    }

    /**
     * Sends a {@link Component} message to the player of
     * the id obtained via construction. If the player is
     * null (offline or does not exist), no message is sent
     * and this method fails silently.
     * 
     * @see Messageable
     * @see Messageable#sendMessage(Component)
     * @see PlayerIdentifier#checkPlayer()
     * @param component the message
     */
    @Override
    public void sendMessage(Component component) {
        Player player = getPlayer();
        if (player != null) {
            player.sendMessage(component);
        }
    }
    
}
