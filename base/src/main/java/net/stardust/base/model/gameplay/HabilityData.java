package net.stardust.base.model.gameplay;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.model.rpg.Level;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class HabilityData implements StardustEntity<UUID> {
	
	@Id
	private UUID id;
	private String habilityId;
	private Level level;
	
	public HabilityData(Player player) {
		this(player.getUniqueId());
	}

	public HabilityData(UUID id) {
		this.id = Objects.requireNonNull(id, "id");
	}

	public void setHabilityId(String habilityId) {
		this.habilityId = habilityId;
	}
	
	public void setLevel(Level level) {
		this.level = level;
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(id);
	}

	@Override
	public UUID getEntityId() {
		return id;
	}
	
}
