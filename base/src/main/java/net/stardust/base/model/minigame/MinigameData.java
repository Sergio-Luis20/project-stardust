package net.stardust.base.model.minigame;

import java.util.HashMap;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.stardust.base.database.BaseEntity;
import net.stardust.base.model.StardustEntity;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@BaseEntity(String.class)
@Entity
@Table(name = "minigame_data")
public class MinigameData implements StardustEntity<String> {
    
    @Id
    @Column(name = "id")
    private String minigameName;

    @Exclude
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "minigame_data_players", 
            joinColumns = @JoinColumn(name = "minigame_data_id", referencedColumnName = "id"), 
            inverseJoinColumns = @JoinColumn(name = "player_id", referencedColumnName = "id")
    )
    @MapKey(name = "id")
    private HashMap<UUID, MinigamePlayer> minigamePlayers;

    public MinigameData(String minigameName) {
        this(minigameName, new HashMap<>());
    }

    @Override
    public String getEntityId() {
        return minigameName;
    }

}
