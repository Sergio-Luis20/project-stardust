package net.stardust.base.model.user;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import net.kyori.adventure.text.Component;
import net.stardust.base.model.Identifier;

/**
 * Represents an object which can serve as identification of a player.
 * 
 * @see Identifier
 * @see Player
 * @see OfflinePlayer
 * @see UUID
 * 
 * @author Sergio Luis
 */
@EqualsAndHashCode
public class PlayerIdentifier implements Identifier<UUID>, Comparable<PlayerIdentifier> {
    
    private final UUID id;

    @Exclude
    private transient Player player;

    @Exclude
    private transient OfflinePlayer offlinePlayer;

    /**
     * Constructs a new PlayerIdentifier based on an {@link UUID}.
     * If the id parameter does not represent a {@link Player} unique
     * id, such as returned by {@link Player#getUniqueId()}, then
     * this class will not work properly.
     * 
     * @see Player
     * @see Player#getUniqueId()
     * @param id the id
     * @throws NullPointerException if id is null
     */
    public PlayerIdentifier(UUID id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    /**
     * Constructs a new PlayerIdentifier based on a {@link Player} unique
     * id, that one returned by {@link Player#getUniqueId()}.
     * 
     * @see Player
     * @see Player#getUniqueId()
     * @param player the player
     * @throws NullPointerException if player is null
     */
    public PlayerIdentifier(Player player) {
        this(player.getUniqueId());
        this.player = player;
    }

    /**
     * Constructs a new PlayerIdentifier base on a {@link OfflinePlayer} unique
     * id, that one returned by {@link OfflinePlayer#getUniqueId()}. If the
     * effective {@link Player} of the offline player object doesn't exist,
     * then this class probably will not work properly.
     * 
     * @param offlinePlayer the offline player
     * @throws NullPointerException if offline player is null
     */
    public PlayerIdentifier(OfflinePlayer offlinePlayer) {
        this(offlinePlayer.getUniqueId());
        this.offlinePlayer = offlinePlayer;
        if (offlinePlayer instanceof Player player) {
            this.player = player;
        }
    }

    /**
     * Returns the internal {@link UUID} object obtained via
     * construction. This is the player id.
     * 
     * @see UUID
     * @see Player#getUniqueId()
     * @see OfflinePlayer#getUniqueId()
     * @return the internal player id ({@link UUID})
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Returns the name of the {@link OfflinePlayer}.
     * 
     * @see OfflinePlayer
     * @see #getComponentName()
     * @return the offline player name
     */
    @Override
    public String getStringName() {
        return getOfflinePlayer().getName();
    }

    /**
     * Return the name of the {@link OfflinePlayer} wrapped in a {@link Component}.
     * 
     * @see Component
     * @see OfflinePlayer
     * @see #getStringName()
     * @return the offline player name wrapped in a {@link Component}
     */
    @Override
    public Component getComponentName() {
        return Component.text(getStringName());
    }

    /**
     * Compares this {@link PlayerIdentifier} with another based
     * on their internal id ({@link UUID}).
     * 
     * @see UUID
     * @see UUID#compareTo(UUID)
     * @return the result of the comparation
     */
    @Override
    public int compareTo(PlayerIdentifier identifier) {
        return id.compareTo(identifier.id);
    }

    /**
     * Returns the lazily loaded player based on the internal id.
     * If the player is offline or does not exist, this method
     * returns null.
     * 
     * @see Bukkit#getPlayer(UUID)
     * @return the player
     */
    public Player getPlayer() {
        if (player == null) {
            player = Bukkit.getPlayer(id);
            if (player != null) {
                offlinePlayer = player;
            }
        }
        return player;
    }

    /**
     * Returns the lazily loaded offline player based on the internal id.
     * 
     * @see Bukkit#getOfflinePlayer(UUID)
     * @return the offline player
     */
    public OfflinePlayer getOfflinePlayer() {
        if (offlinePlayer == null) {
            offlinePlayer = Bukkit.getOfflinePlayer(id);
            if (offlinePlayer instanceof Player player) {
                this.player = player;
            }
        }
        return offlinePlayer;
    }
    
    /**
     * Utility method for checking if the player is valid, that is,
     * if it exists and is online. If those conditions doesn't met,
     * this method throws an {@link InvalidPlayerException}.
     * 
     * @see Player
     * @see Bukkit#getPlayer(String)
     * @see Bukkit#getPlayer(UUID)
     * @see InvalidPlayerException
     * @return the player if valid
     * @throws InvalidPlayerException if the player is invalid
     */
    public Player checkPlayer() {
        Player player = getPlayer();
        if (player == null) {
            throw InvalidPlayerException.builder()
                    .message("Player does not exist or is offline")
                    .id(id)
                    .build();
        }
        return player;
    }

}
