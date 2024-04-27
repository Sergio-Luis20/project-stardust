package net.stardust.base.model.economy.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;

import net.stardust.base.model.economy.MonetaryEntity;
import net.stardust.base.utils.persistence.DataCaster;
import net.stardust.base.utils.persistence.DataManager;
import net.stardust.base.utils.persistence.KeyMapper;
import net.stardust.base.utils.persistence.NonRepresentativeDataException;
import net.stardust.base.utils.persistence.StructureChecker;

public class CheckDataCaster implements DataCaster<Check, ItemMeta> {

    private KeyMapper keyMapper = () -> {
        Map<NamespacedKey, Class<?>> map = new HashMap<>();
        map.put(new NamespacedKey("stardust", "check-owner-id"), UUID.class);
        map.put(new NamespacedKey("stardust", "check-money"), MonetaryEntity.class);
        return map;
    };
    private StructureChecker checker = new StructureChecker(keyMapper);

    @Override
    public Check cast(DataManager<ItemMeta> dataManager) throws NonRepresentativeDataException {
        if(!checker.check(dataManager)) throw new NonRepresentativeDataException();
        UUID playerId = dataManager.readUUID("stardust:check-owner-id");
        MonetaryEntity money = dataManager.readObject("stardust:check-money", MonetaryEntity.class);
        return new Check(playerId, money);
    }

    @Override
    public void record(Check obj, DataManager<ItemMeta> dataManager) {
        dataManager.writeObject("stardust:check-owner-id", obj.getId());
        dataManager.writeObject("check-money", obj.getMoney());
    }
    
}
