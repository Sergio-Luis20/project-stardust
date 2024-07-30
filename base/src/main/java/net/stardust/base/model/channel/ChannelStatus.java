package net.stardust.base.model.channel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.database.BaseEntity;

@Getter
@EqualsAndHashCode
@BaseEntity(UUID.class)
@Entity
public class ChannelStatus implements StardustEntity<UUID>, Cloneable {
    
    @Id
    private UUID id;

    // Key: channel class name. Value: places where it is activated.
    @Getter
    private Map<String, Map<String, Boolean>> properties;

    public ChannelStatus(UUID id) {
        this(id, new HashMap<>());
    }

    public ChannelStatus(UUID id, Map<String, Map<String, Boolean>> properties) {
        this.id = Objects.requireNonNull(id, "id");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public boolean isChannelActivated(String channelClassName, String place) {
        Map<String, Boolean> props = properties.get(channelClassName);
        if (props == null) {
            ChannelNotFoundException ex = new ChannelNotFoundException();
            ex.setChannelClassName(channelClassName);
            throw ex;
        }
        return props.getOrDefault(place, false);
    }

    @Override
    public ChannelStatus clone() {
        Map<String, Map<String, Boolean>> properties = new HashMap<>(this.properties.size());
        properties.forEach((channelClassName, effectiveProps) -> properties.put(channelClassName,
                new HashMap<>(effectiveProps)));
        UUID newId = new UUID(id.getMostSignificantBits(), id.getLeastSignificantBits());
        return new ChannelStatus(newId, properties);
    }

    @Override
    public UUID getEntityId() {
        return id;
    }

}
