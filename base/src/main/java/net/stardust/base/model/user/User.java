package net.stardust.base.model.user;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.stardust.base.database.BaseEntity;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.security.PasswordEncryption;
import net.stardust.base.utils.security.PasswordException;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@BaseEntity(UUID.class)
@Entity
@Table(name = "users")
public class User implements StardustEntity<UUID>, Cloneable, PlayerIdentifierProvider {
    
    @Id
    private UUID id;
    private long registered;

    @Setter
    private String name, email;

    private byte[] salt, password;

    @Builder
    public User(UUID id, long registered, String name, String email, String password) throws PasswordException {
        this(id, registered, name, email);
        setPassword(password);
    }

    private User(UUID id, long registered, String name, String email) {
        this.id = id;
        this.registered = registered;
        this.name = name;
        this.email = email;
    }

    public byte[] getSalt() {
        return salt.clone();
    }

    public byte[] getPassword() {
        return password.clone();
    }

    public void setPassword(String password) throws PasswordException {
        byte[] salt = PasswordEncryption.generateSalt();
        byte[] pw = PasswordEncryption.generateHash(password, salt);
        this.salt = salt;
        this.password = pw;
    }

    @Override
    public User clone() {
        User clone = new User(id, registered, name, email);
        clone.salt = salt;
        clone.password = password;
        return clone;
    }

    @Override
    public UUID getEntityId() {
        return id;
    }

    @Override
    public PlayerIdentifier getIdentifier() {
        return new PlayerIdentifier(id);
    }

}
