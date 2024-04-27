package net.stardust.base.model.terrain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Home implements Serializable {

    private UUID id;
    private String name;
    private transient Location location;
    private transient OfflinePlayer offlinePlayer;
    private transient Player player;

    public Home(UUID id, String name, Location location) {
        setId(id);
        setName(name);
        setLocation(location);
    }
    
    public Home(Player player, String name, Location location) {
        this(player.getUniqueId(), name, location);
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

    public OfflinePlayer getOfflinePlayer() {
        if(offlinePlayer == null) {
            offlinePlayer = Bukkit.getOfflinePlayer(id);
        }
        return offlinePlayer;
    }

    public Player getPlayer() {
        if(player == null) {
            player = Bukkit.getPlayer(id);
        }
        return player;
    }

    public void setId(UUID id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public void setLocation(Location location) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(location.getWorld(), "location.world");
        this.location = location;
    }

}
