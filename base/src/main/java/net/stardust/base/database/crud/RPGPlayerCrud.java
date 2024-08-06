package net.stardust.base.database.crud;

import java.util.UUID;

import net.stardust.base.model.rpg.RPGPlayer;

public class RPGPlayerCrud extends Crud<UUID, RPGPlayer> {

    public RPGPlayerCrud() {
        super(RPGPlayer.class);
    }
    
}
