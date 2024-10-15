package net.stardust.base.sequence.room.wave;

import java.util.Arrays;

import net.stardust.base.sequence.DungeonEnemy;

public class Wave {
    
    private DungeonEnemy[] enemies;

    public Wave(DungeonEnemy... enemies) {
        this.enemies = Arrays.copyOf(enemies, enemies.length);
    }

    public DungeonEnemy[] getEnemies() {
        return enemies;
    }

}
