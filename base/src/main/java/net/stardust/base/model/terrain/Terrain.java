package net.stardust.base.model.terrain;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import br.sergio.utils.Pair;
import br.sergio.utils.math.Point;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.database.BaseEntity;

@Getter
@EqualsAndHashCode
@BaseEntity(UUID.class)
@Entity
public class Terrain implements StardustEntity<UUID> {

    @Id
    private UUID id;
    private String terrainWorldName;
    private Pair<Point, Point> terrainDelimiter;

    @Enumerated
    private TerrainSize size;

    @Transient
    private transient World terrainWorld;

    public Terrain(UUID id, String terrainWorldName, Pair<Point, Point> terrainDelimiter, TerrainSize size) {
        this.id = Objects.requireNonNull(id, "id");
        this.terrainWorldName = Objects.requireNonNull(terrainWorldName, "terrainWorldName");
        this.terrainDelimiter = Objects.requireNonNull(terrainDelimiter, "terrainDelimiter");
        this.size = Objects.requireNonNull(size, "size");
        if(!terrainWorldName.startsWith("terrain")) {
            throw new IllegalArgumentException("terrain world name is not a terrain world");
        }
        if(terrainDelimiter.test((firstPoint, secondPoint) -> firstPoint == null || secondPoint == null)) {
            throw new IllegalArgumentException("point in terrain delimiter cannot be null");
        }
        if(terrainDelimiter.test(Objects::equals)) {
            throw new IllegalArgumentException("points in terrain delimiter cannot be the same point");
        }
        if(terrainDelimiter.test((firstPoint, secondPoint) -> firstPoint.getY() != secondPoint.getY())) {
            throw new IllegalArgumentException("points in terrain delimiter must be at same height (y)");
        }
    }

    public Terrain(UUID id, World terrainWorld, Pair<Point, Point> terrainDelimiter, TerrainSize size) {
        this(id, Objects.requireNonNull(terrainWorld, "terrainWorld").getName(), terrainDelimiter, size);
        this.terrainWorld = terrainWorld;
    }

    public Pair<Point, Point> getTerrainDelimiter() {
        return terrainDelimiter.clone();
    }

    public World getTerrainWorld() {
        if(terrainWorld == null) {
            terrainWorld = Bukkit.getWorld(terrainWorldName);
        }
        return terrainWorld;
    }

    @Override
    public UUID getEntityId() {
        return id;
    }

}
