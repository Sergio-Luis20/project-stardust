package net.stardust.base.model.economy.storage;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import org.bukkit.inventory.ItemStack;

/**
 * The server storage doesn't store anything. It is a phantom
 * object since the server generates items automatically.
 * 
 * @see Storage
 * @author Sergio Luis
 */
public class ServerStorage implements Storage, Serializable {

    /**
     * The singleton {@link ServerStorage} instance.
     */
    public static final ServerStorage INSTANCE = new ServerStorage();

    private ServerStorage() {
    }

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
    public boolean canStore(List<ItemStack> items) {
        return true;
    }

    @Serial
    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
    
}
