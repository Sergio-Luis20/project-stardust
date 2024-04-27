package net.stardust.base.model.economy.shop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Location;

import net.stardust.base.model.economy.owner.PlayerShopOwner;

public class PlayerShop implements Shop, Serializable {

    private PlayerShopOwner owner;
    private String name;
    private Location location;

    public PlayerShop(PlayerShopOwner owner, String name, Location location) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.name = Objects.requireNonNull(name, "name");
        this.location = Objects.requireNonNull(location, "location");
    }

    @Override
    public PlayerShopOwner getOwner() {
        return owner;
    }

    @Override
    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o == this) return true;
        if(o instanceof PlayerShop shop) {
            return owner.equals(shop.owner) && name.equals(shop.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name);
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(location.serialize());
    }

    @Serial
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        location = Location.deserialize((Map<String, Object>) in.readObject());
    }
    
}
