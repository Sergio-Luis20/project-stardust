package net.stardust.base.model.economy.wallet;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.database.BaseEntity;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@BaseEntity(UUID.class)
@Entity
@Table(name = "player_wallet")
public class PlayerWallet extends Wallet implements StardustEntity<UUID> {
    
    @Id
    private UUID id;

    public PlayerWallet(Player player) {
        super();
        id = player.getUniqueId();
    }

    public PlayerWallet(Player player, long bronze, long silver, long gold) {
        super(bronze, silver, gold);
        id = player.getUniqueId();
    }

    public PlayerWallet(Player player, BigInteger bronze, BigInteger silver, BigInteger gold) {
        super(bronze, silver, gold);
        id = player.getUniqueId();
    }

    public PlayerWallet(UUID id) {
        super();
        this.id = Objects.requireNonNull(id, "id");
    }

    public PlayerWallet(UUID id, long bronze, long silver, long gold) {
        super(bronze, silver, gold);
        this.id = Objects.requireNonNull(id, "id");
    }

    public PlayerWallet(UUID id, BigInteger bronze, BigInteger silver, BigInteger gold) {
        super(bronze, silver, gold);
        this.id = Objects.requireNonNull(id, "id");
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(id);
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(id);
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
