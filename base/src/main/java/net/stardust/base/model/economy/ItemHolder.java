package net.stardust.base.model.economy;

import net.stardust.base.model.economy.storage.Storage;

public interface ItemHolder extends Cash {
    
    Storage getStorage();

}
