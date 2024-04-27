package net.stardust.base.model.rpg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
public abstract class PlayerAttribute implements Serializable {
    
    protected String generalName;
    protected Level level;
    protected Map<String, Multiplier> multipliers;

    public PlayerAttribute(String generalName) {
        this(generalName, null, null);
    }

    public PlayerAttribute(String generalName, Level level, Map<String, Multiplier> multipliers) {
        this.generalName = Objects.requireNonNull(generalName, "generalName");
        setLevel(level);
        setMultipliers(multipliers);
    }

    public void setLevel(Level level) {
        this.level = level == null ? new Level() : level;
    }

    public void setMultipliers(Map<String, Multiplier> multipliers) {
        this.multipliers = multipliers == null ? new HashMap<>() : multipliers;
    }

    public abstract String getName(ResourceBundle bundle);
    public abstract String getDescription(ResourceBundle bundle);

}
