package net.stardust.base.model.rpg;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.entity.Player;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.model.gameplay.Rank;
import net.stardust.base.utils.database.BaseEntity;

@Getter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@BaseEntity(UUID.class)
public class RPGPlayer implements StardustEntity<UUID> {

    @Id
    private UUID id;
    private Rank rank;
    
    private Map<String, PlayerAttribute> attributes;
    private Map<String, Skill> skills;

    public RPGPlayer(Player player) {
        this(player.getUniqueId());
    }

    public RPGPlayer(UUID id) {
        this(id, Rank.E, new HashMap<>(), new HashMap<>());
    }

    public RPGPlayer(Player player, Rank rank, Map<String, PlayerAttribute> attributes, Map<String, Skill> skills) {
        this(player.getUniqueId(), rank, attributes, skills);
    }

    public RPGPlayer(UUID id, Rank rank, Map<String, PlayerAttribute> attributes, Map<String, Skill> skills) {
        this.id = Objects.requireNonNull(id);
        setRank(rank);
        setAttributes(attributes);
        setSkills(skills);
    }

    public void setRank(Rank rank) {
        this.rank = Objects.requireNonNull(rank, "rank = null");
    }

    public void setAttributes(Map<String, PlayerAttribute> attributes) {
        this.attributes = Objects.requireNonNull(attributes, "attributes");
    }

    public void setSkills(Map<String, Skill> skills) {
        this.skills = Objects.requireNonNull(skills, "skills");
    }

    @Override
    public UUID getEntityId() {
        return id;
    }

}
