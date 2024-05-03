package net.stardust.base.events;

import net.stardust.base.Communicable;
import org.bukkit.World;
import org.bukkit.event.Listener;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class WorldListener implements Listener, Communicable {

    private Supplier<World> supplier;

    public WorldListener(Supplier<World> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    public boolean checkWorld(World world) {
        return getWorld().equals(world);
    }

    public World getWorld() {
        return supplier.get();
    }

}
