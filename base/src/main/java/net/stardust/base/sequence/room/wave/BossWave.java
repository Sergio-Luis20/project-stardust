package net.stardust.base.sequence.room.wave;

import java.util.Objects;

import net.stardust.base.sequence.DungeonBoss;

public class BossWave extends Wave {
    
    private DungeonBoss boss;

    public BossWave(DungeonBoss boss) {
        super(Objects.requireNonNull(boss, "boss"));
        this.boss = boss;
    }

    public DungeonBoss getBoss() {
        return boss;
    }

}
