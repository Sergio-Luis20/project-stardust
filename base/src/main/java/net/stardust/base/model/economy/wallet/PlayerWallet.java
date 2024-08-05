package net.stardust.base.model.economy.wallet;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.model.user.PlayerIdentifier;
import net.stardust.base.model.user.PlayerIdentifierProvider;
import net.stardust.base.utils.database.BaseEntity;

/**
 * This class represents the wallet of a player in Stardust server.
 * 
 * @see Wallet
 * 
 * @author Sergio Luis
 */
@EqualsAndHashCode(callSuper = true)
@BaseEntity(UUID.class)
@Entity
@Table(name = "player_wallet")
public class PlayerWallet extends Wallet implements PlayerIdentifierProvider, StardustEntity<UUID> {
    
    @Id
    private UUID id;

    @Transient
    private transient PlayerIdentifier identifier;

    /**
     * Constructs an empty PlayerWallet with no player id. This constructor
     * is only used by internal systems to instantiate this class more easily,
     * and thus must never be used manually. Use any of the other constructors
     * to create a PlayerWallet object.
     * 
     * @see #PlayerWallet(Player)
     * @see #PlayerWallet(Player, BigInteger, BigInteger, BigInteger)
     * @see #PlayerWallet(Player, long, long, long)
     * @see #PlayerWallet(UUID)
     * @see #PlayerWallet(UUID, BigInteger, BigInteger, BigInteger)
     * @see #PlayerWallet(UUID, long, long, long)
     */
    public PlayerWallet() {
        super();
    }

    /**
     * Constructs a PlayerWallet based on a player id. See {@link Wallet}
     * documention to know how empty initialization is treated.
     * 
     * @see Wallet
     * @see Wallet#Wallet()
     * @see #PlayerWallet(Player, BigInteger, BigInteger, BigInteger)
     * @see #PlayerWallet(Player, long, long, long)
     * @see #PlayerWallet(UUID)
     * @see #PlayerWallet(UUID, BigInteger, BigInteger, BigInteger)
     * @see #PlayerWallet(UUID, long, long, long)
     * @param player the player
     * @throws NullPointerException if player is null
     */
    public PlayerWallet(Player player) {
        super();
        this.id = player.getUniqueId();
    }

    /**
     * Constructs a PlayerWallet based on a player id and initial values
     * for bronze, silver and gold currencies as long. See {@link Wallet}
     * documentation to know how this is treated.
     * 
     * @see Wallet
     * @see Wallet#Wallet(long, long, long)
     * @see #PlayerWallet(Player)
     * @see #PlayerWallet(Player, BigInteger, BigInteger, BigInteger)
     * @see #PlayerWallet(UUID)
     * @see #PlayerWallet(UUID, BigInteger, BigInteger, BigInteger)
     * @see #PlayerWallet(UUID, long, long, long)
     * @param player the player
     * @param bronze the bronze value
     * @param silver the silver value
     * @param gold the gold value
     * @throws NullPointerException if player is null
     */
    public PlayerWallet(Player player, long bronze, long silver, long gold) {
        super(bronze, silver, gold);
        this.id = player.getUniqueId();
    }

    /**
     * Constructs a PlayerWallet based on a player id and initial values
     * fro bronze, silver and gold currencies as {@link BigInteger}s. See
     * {@link Wallet} documentation to know how this is treated.
     * 
     * @see Wallet
     * @see Wallet#Wallet(BigInteger, BigInteger, BigInteger)
     * @see #PlayerWallet(Player)
     * @see #PlayerWallet(Player, long, long, long)
     * @see #PlayerWallet(UUID)
     * @see #PlayerWallet(UUID, BigInteger, BigInteger, BigInteger)
     * @see #PlayerWallet(UUID, long, long, long)
     * @param player the player
     * @param bronze the bronze value
     * @param silver the silver value
     * @param gold the gold value
     * @throws NullPointerException if player is null
     */
    public PlayerWallet(Player player, BigInteger bronze, BigInteger silver, BigInteger gold) {
        super(bronze, silver, gold);
        this.id = player.getUniqueId();
    }

    /**
     * Constructs a PlayerWallet based on an {@link UUID}. See {@link Wallet}
     * documention to know how empty initialization is treated. If the
     * id passed is not a {@link Player} unique id (as returned by
     * {@link Player#getUniqueId()}), then this class probably will not
     * work properly.
     * 
     * @see Wallet
     * @see Wallet#Wallet()
     * @see #PlayerWallet(Player)
     * @see #PlayerWallet(Player, BigInteger, BigInteger, BigInteger)
     * @see #PlayerWallet(Player, long, long, long)
     * @see #PlayerWallet(UUID, BigInteger, BigInteger, BigInteger)
     * @see #PlayerWallet(UUID, long, long, long)
     * @param id the id
     * @throws NullPointerException if id is null
     */
    public PlayerWallet(UUID id) {
        super();
        this.id = Objects.requireNonNull(id, "id");
    }

    /**
     * Constructs a PlayerWallet based on an {@link UUID} and initial
     * values for bronze, silver and gold currencies as long. See {@link Wallet}
     * documentation to know how this is treated. If the id passed is not a
     * {@link Player} unique id (as returned by {@link Player#getUniqueId()}),
     * then this class probably will not work properly.
     * 
     * @see Wallet
     * @see Wallet#Wallet(long, long, long)
     * @see #PlayerWallet(Player)
     * @see #PlayerWallet(Player, BigInteger, BigInteger, BigInteger)
     * @see #PlayerWallet(Player, long, long, long)
     * @see #PlayerWallet(UUID)
     * @see #PlayerWallet(UUID, BigInteger, BigInteger, BigInteger)
     * @param id the id
     * @param bronze the bronze value
     * @param silver the silver value
     * @param gold the gold value
     * @throws NullPointerException if id is null
     */
    public PlayerWallet(UUID id, long bronze, long silver, long gold) {
        super(bronze, silver, gold);
        this.id = Objects.requireNonNull(id, "id");
    }

    /**
     * Constructs a PlayerWallet based on an {@link UUID} and initial
     * values for bronze, silver and gold currencies as {@link BigInteger}s.
     * See {@link Wallet} documentation to know how this is treated. If the
     * id passed is not a {@link Player} unique id (as returned by
     * {@link Player#getUniqueId()}), then this class probably will not
     * work properly.
     * 
     * @see Wallet
     * @see Wallet#Wallet(BigInteger, BigInteger, BigInteger)
     * @see #PlayerWallet(Player)
     * @see #PlayerWallet(Player, BigInteger, BigInteger, BigInteger)
     * @see #PlayerWallet(Player, long, long, long)
     * @see #PlayerWallet(UUID)
     * @see #PlayerWallet(UUID, long, long, long)
     * @param id the id
     * @param bronze the bronze value
     * @param silver the silver value
     * @param gold the gold value
     * @throws NullPointerException if id is null
     */
    public PlayerWallet(UUID id, BigInteger bronze, BigInteger silver, BigInteger gold) {
        super(bronze, silver, gold);
        this.id = Objects.requireNonNull(id, "id");
    }

    /**
     * Returns a {@link PlayerIdentifier} object from
     * this PlayerWallet, using the {@link UUID} obtained
     * in constructors as the id of the identifier.
     * 
     * @see PlayerIdentifier
     * @see PlayerIdentifierProvider
     * @return a {@link PlayerIdentifier} from this object
     */
    @Override
    public PlayerIdentifier getIdentifier() {
        if (identifier == null) {
            identifier = new PlayerIdentifier(id);
        }
        return identifier;
    }

    public Player getPlayer() {
        return getIdentifier().getPlayer();
    }

    public OfflinePlayer getOfflinePlayer() {
        return getIdentifier().getOfflinePlayer();
    }

    public UUID getId() {
        return id;
    }

    @Override
    public PlayerWallet getWallet() {
        return this;
    }

    @Override
    public PlayerWallet clone() {
        return new PlayerWallet(id, getBronze().getValue(), getSilver().getValue(), getGold().getValue());
    }

    @Override
    public UUID getEntityId() {
        return id;
    }

}
