package net.stardust.minigames.capture;

import org.bukkit.Location;

import java.util.function.Function;

public enum CaptureLocation {

    SPAWN("spawn", Capture::getSpawn),
    BLUEBASE("blue-base", Capture::getBlueBase),
    REDBASE("red-base", Capture::getRedBase);

    public final String yamlKeyName;
    private final Function<Capture, Location> locationFunction;

    CaptureLocation(String yamlKeyName, Function<Capture, Location> locationFunction) {
        this.yamlKeyName = yamlKeyName;
        this.locationFunction = locationFunction;
    }

    public Location toLocation(Capture capture) {
        return locationFunction.apply(capture);
    }

}
