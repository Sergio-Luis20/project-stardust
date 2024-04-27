package net.stardust.base.utils;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public record SoundPack(Sound sound, float volume, float pitch) {

    public SoundPack {
        Objects.requireNonNull(sound, "sound");
        if(volume < 0 || volume > 1) throw new IllegalArgumentException("volume must be in range [0, 1]");
        if(pitch < 0 || pitch > 2) throw new IllegalArgumentException("pitch must be in range [0, 2]");
    }

    public SoundPack(Sound sound, float pitch) {
        this(sound, 1, pitch);
    }

    public SoundPack(Sound sound) {
        this(sound, 1);
    }

    public void play(Player player) {
        player.playSound(player, sound, volume, pitch);
    }

    public void play(Player player, Location location) {
        player.playSound(location, sound, volume, pitch);
    }

    public void play(Player player, Entity entity) {
        player.playSound(entity, sound, volume, pitch);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o == this) return true;
        if(o instanceof SoundPack sp) {
            return sound == sp.sound && volume == sp.volume && pitch == sp.pitch;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sound, volume, pitch);
    }
    
}
