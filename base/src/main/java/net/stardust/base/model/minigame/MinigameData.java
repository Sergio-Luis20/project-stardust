package net.stardust.base.model.minigame;

import java.util.HashMap;
import java.util.UUID;

import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.database.BaseEntity;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
@BaseEntity(String.class)
public class MinigameData implements StardustEntity<String> {
    
    @Id
    @NonNull
    private String minigameName;

    @NonNull
    @Exclude private HashMap<UUID, MinigamePlayer> minigamePlayers;

    public MinigameData(String minigameName) {
        this(minigameName, new HashMap<>());
    }

    @Override
    public String getEntityId() {
        return minigameName;
    }

}
