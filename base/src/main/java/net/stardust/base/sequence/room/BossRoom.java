package net.stardust.base.sequence.room;

import java.util.Objects;

import net.stardust.base.sequence.room.wave.BossWave;

public class BossRoom extends Room {
    
    private BossWave bossWave;

    public BossRoom(BossWave bossWave) {
        super(Objects.requireNonNull(bossWave, "bossWave"));
        this.bossWave = bossWave;
    }

    public BossWave getBossWave() {
        return bossWave;
    }

}
