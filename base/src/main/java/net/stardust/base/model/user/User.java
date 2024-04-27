package net.stardust.base.model.user;

import java.util.UUID;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.database.BaseEntity;

@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@BaseEntity(UUID.class)
public class User implements StardustEntity<UUID> {
    
    @Id
    private UUID id;
    private long registered;
    @Exclude private String name, email;
    @Exclude private byte[] salt, password;

    @Override
    public UUID getEntityId() {
        return id;
    }

}
