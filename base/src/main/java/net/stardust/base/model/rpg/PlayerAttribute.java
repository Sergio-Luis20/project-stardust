package net.stardust.base.model.rpg;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.stardust.base.model.Nameable;
import net.stardust.base.utils.database.BinaryConverter;
import net.stardust.base.utils.database.lang.Translation;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class PlayerAttribute implements Serializable, Nameable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;

    @Lob
    @Convert(converter = BinaryConverter.class)
    private Level level;

    @Lob
    @Convert(converter = BinaryConverter.class)
    private NumberComposition multipliers;
    
    private String nameTranslationKey, descriptionTranslationKey;
    private float baseFactor;

    public PlayerAttribute(String name) {
        this(name, null, 1, null);
    }

    public PlayerAttribute(String name, Level level, float baseFactor, NumberComposition multipliers) {
        this.name = Objects.requireNonNull(name, "name");
        setLevel(level);
        setBaseFactor(baseFactor);
        setMultipliers(multipliers);
    }

    public void setLevel(Level level) {
        this.level = Objects.requireNonNull(level, "level");
    }

    public void setMultipliers(NumberComposition multipliers) {
        this.multipliers = multipliers;
    }

    protected void setBaseFactor(float baseFactor) {
        if (baseFactor <= 0) {
            throw new IllegalArgumentException("base factor cannot be 0 or negative");
        }
        this.baseFactor = baseFactor;
    }

    public float getRawValue() {
        return baseFactor * level.getValue();
    }

    public float getValue() {
        return multipliers == null ? getRawValue() : multipliers.function(getRawValue());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getName(Locale locale) {
        return Translation.string(locale, nameTranslationKey);
    }

    public String getDescription(Locale locale) {
        return Translation.string(locale, descriptionTranslationKey);
    }

}
