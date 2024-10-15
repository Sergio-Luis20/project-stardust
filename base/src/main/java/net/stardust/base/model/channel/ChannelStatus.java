package net.stardust.base.model.channel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.stardust.base.database.BaseEntity;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.ObjectMapperFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@BaseEntity(UUID.class)
@NoArgsConstructor
@Entity
public class ChannelStatus implements StardustEntity<UUID>, Cloneable {
    
    @Id
    private UUID id;

    // Key: channel class name. Value: places where it is activated.
    @Getter
    @Transient
    private Map<String, Map<String, Boolean>> properties;

    @Column(columnDefinition = "TEXT")
    private String json;

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

    @PrePersist
    private void convertToJson() throws JsonProcessingException {
        json = ObjectMapperFactory.getDefault().writeValueAsString(properties);
    }

    @PostLoad
    private void convertToMap() throws JsonProcessingException {
        properties = ObjectMapperFactory.getDefault().readValue(json, new TypeReference<>() {});
        json = null;
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
