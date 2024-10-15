package net.stardust.base.sequence.room;

import java.util.Arrays;

import net.stardust.base.sequence.room.wave.Wave;

public class Room {
    
    private Wave[] waves;

    public Room(Wave... waves) {
        this.waves = Arrays.copyOf(waves, waves.length);
    }

    public Wave[] getWaves() {
        return waves.clone();
    }

}
