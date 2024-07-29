package net.stardust.base.model.user;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.database.BaseEntity;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@BaseEntity(UUID.class)
@Entity
@Table(name = "users")
public class User implements StardustEntity<UUID>, Cloneable {
    
    @Id
    private UUID id;
    private long registered;
    private String name, email;
    private byte[] salt, password;

    @Override
    public User clone() {
        return new User(id, registered, name, email, salt, password);
    }

    @Override
    public UUID getEntityId() {
        return id;
    }

}
