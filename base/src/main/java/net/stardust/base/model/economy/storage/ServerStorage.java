package net.stardust.base.model.economy.storage;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;

import org.bukkit.inventory.ItemStack;

/**
 * The server storage doesn't store anything. It is a phantom
 * object since the server generates items automatically.
 * 
 * @author Sergio Luis
 */
public class ServerStorage implements Storage, Serializable {

    public static final ServerStorage INSTANCE = new ServerStorage();

    private ServerStorage() {}

    @Override
    public boolean addItem(ItemStack item) {
        return true;
    }

    @Override
    public boolean removeItem(ItemStack item) {
        return true;
    }

    @Override
    public boolean hasItem(ItemStack item) {
        return true;
    }

    @Override
    public int remainingCapacity() {
        return -1;
    }

    @Serial
    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
    
}
