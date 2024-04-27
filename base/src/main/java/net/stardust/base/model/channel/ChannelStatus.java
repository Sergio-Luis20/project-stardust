package net.stardust.base.model.channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.database.BaseEntity;
import net.stardust.base.utils.property.Property;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
@NoArgsConstructor
@BaseEntity(UUID.class)
public class ChannelStatus implements StardustEntity<UUID>, Cloneable {
    
    @Id
    @NonNull
    private UUID id;

    @Getter
    @NonNull
    private Map<String, Set<Property>> properties;

    public ChannelStatus(UUID id) {
        this(id, new HashMap<>());
    }

    public Property getProperty(String channelName, String propertyName) {
        Set<Property> props = properties.get(channelName);
        if(props == null) {
            return null;
        }
        for(Property prop : props) {
            if(propertyName.equals(prop.getName())) {
                return prop;
            }
        }
        return null;
    }

    @Override
    public ChannelStatus clone() {
        ChannelStatus copy = new ChannelStatus(id, new HashMap<>(properties.size()));
        properties.forEach((channelName, props) -> {
            Set<Property> propsCopy = new HashSet<>(props.size());
            props.forEach(prop -> propsCopy.add(prop.clone()));
            copy.properties.put(channelName, propsCopy);
        });
        return copy;
    }

    @Override
    public UUID getEntityId() {
        return id;
    }

}
