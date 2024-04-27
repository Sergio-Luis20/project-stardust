package net.stardust.base.model.economy.transaction;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.stardust.base.model.economy.storage.Storage;

public class AlternativeStoragePlayerItemHolder extends PlayerItemHolder {

    private Storage storage;

    public AlternativeStoragePlayerItemHolder(UUID id, Storage storage) {
        super(id);
        this.storage = Objects.requireNonNull(storage, "storage");
    }

    public AlternativeStoragePlayerItemHolder(Player player, Storage storage) {
        this(player.getUniqueId(), storage);
    }

    @Override
    public Storage getStorage() {
        return storage;
    }
    
}
