package net.stardust.minigames.signs;

import lombok.Getter;
import org.bukkit.Location;

@Getter
public class SignWithoutMatchException extends RuntimeException {

    private Location location;

    public SignWithoutMatchException(Location location) {
        super("Sign location: " + location);
        this.location = location;
    }

}
