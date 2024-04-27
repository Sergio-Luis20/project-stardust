package net.stardust.base.model.economy.sign;

import org.bukkit.block.Sign;

import net.stardust.base.utils.persistence.DataCaster;
import net.stardust.base.utils.persistence.DataManager;
import net.stardust.base.utils.persistence.NonRepresentativeDataException;

public class SignCaster implements DataCaster<SignUnitData, Sign> {

    private static final String KEY = "stardust:sign_unit_data";

    @Override
    public SignUnitData cast(DataManager<Sign> dataManager) throws NonRepresentativeDataException {
        try {
            return dataManager.readObject(KEY, SignUnitData.class);
        } catch(ClassCastException e) {
            throw new NonRepresentativeDataException("not a shop sign", e);
        }
    }

    @Override
    public void record(SignUnitData obj, DataManager<Sign> dataManager) {
        dataManager.writeObject(KEY, obj);
    }
    
}
