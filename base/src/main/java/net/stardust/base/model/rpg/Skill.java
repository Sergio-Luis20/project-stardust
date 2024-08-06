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
import lombok.NoArgsConstructor;
import net.stardust.base.database.BinaryConverter;
import net.stardust.base.database.lang.Translation;
import net.stardust.base.model.Nameable;

@NoArgsConstructor
@Entity
public class Skill implements Serializable, Nameable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob
    @Convert(converter = BinaryConverter.class)
    private Level level;
    
    private String nameTranslationKey, descriptionTranslationKey;

    public Skill(String name) {
        this.name = Objects.requireNonNull(name, "name");
        this.level = new Level();
    }

    public String getNameTranslationKey() {
        return nameTranslationKey;
    }

    public void setNameTranslationKey(String nameTranslationKey) {
        this.nameTranslationKey = nameTranslationKey;
    }

    public String getDescriptionTranslationKey() {
        return descriptionTranslationKey;
    }

    public void setDescriptionTranslationKey(String descriptionTranslationKey) {
        this.descriptionTranslationKey = descriptionTranslationKey;
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

    public Level getLevel() {
        return level;
    }

}
