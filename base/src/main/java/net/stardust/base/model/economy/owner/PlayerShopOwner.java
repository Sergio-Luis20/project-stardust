package net.stardust.base.model.economy.owner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.stardust.base.database.BaseEntity;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.model.economy.shop.PlayerShop;
import net.stardust.base.model.economy.shop.Shop;

@BaseEntity(UUID.class)
public class PlayerShopOwner implements ShopOwner, StardustEntity<UUID> {

    private UUID playerId;
    private transient Player player;

    private Set<PlayerShop> shops;

    public PlayerShopOwner(UUID playerId) {
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        shops = new HashSet<>();
    }

    public PlayerShopOwner(Player player) {
        this(player.getUniqueId());
        this.player = player;
    }

    public boolean addShop(String name, Location location) {
        var shop = new PlayerShop(this, name, location);
        if(!shops.contains(shop)) {
            return shops.add(shop);
        }
        return false;
    }

    public boolean removeShop(String name) {
        PlayerShop obj = getShop(name);
        if(obj != null) {
            return shops.remove(obj);
        }
        return false;
    }

    public PlayerShop getShop(String name) {
        PlayerShop obj = null;
        for(var shop : shops) {
            if(shop.getName().equals(name)) {
                obj = shop;
                break;
            }
        }
        return obj;
    }

    public boolean hasShop(String name) {
        return getShop(name) != null;
    }

    @Override
    public Set<? extends Shop> getShops() {
        return Collections.unmodifiableSet(shops);
    }

    public Player getPlayer() {
        if(player == null) {
            player = Bukkit.getPlayer(playerId);
        }
        return player;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o == this) return true;
        if(o instanceof PlayerShopOwner pso) {
            return playerId.equals(pso.playerId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return playerId.hashCode();
    }

    @Override
    public UUID getEntityId() {
        return playerId;
    }
    
}
