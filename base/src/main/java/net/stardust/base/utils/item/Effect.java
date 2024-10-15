package net.stardust.base.utils.item;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.experimental.StandardException;
import net.stardust.base.Stardust;
import net.stardust.base.utils.ObjectMapperFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectType.Category;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public record Effect(PotionEffect effect, boolean overwrite) {

    public static final Map<String, EffectData> DATA;

    public static void main(String[] args) throws JsonProcessingException {
        var mapper = ObjectMapperFactory.getDefault();
        var printer = mapper.writerWithDefaultPrettyPrinter();
        System.out.println(printer.writeValueAsString(DATA));
    }

    public Effect {
        Objects.requireNonNull(effect, "effect");
    }

    public Effect(PotionEffect effect) {
        this(effect, true);
    }

    public void addTo(PotionMeta meta) {
        meta.addCustomEffect(effect, overwrite);
    }

    public static Effect instant(PotionEffectType type) {
        return instant(type, 1);
    }

    public static Effect instant(PotionEffectType type, int amplifier) {
        if (!type.isInstant()) {
            throw new IllegalArgumentException("Type " + type.getKey().getKey() + " is not instant");
        }
        if (amplifier <= 0) {
            throw new IllegalArgumentException("Amplifier must be strictly positive, but it was: " + amplifier);
        }
        return new Effect(type.createEffect(0, amplifier));
    }

    public static Effect normal(PotionEffectType type) {
        return createEffect(type, TimeValue::normal, "normal", 1);
    }

    public static Effect extended(PotionEffectType type) {
        return createEffect(type, TimeValue::extended, "extended", 1);
    }

    public static Effect amplified(PotionEffectType type) {
        return createEffect(type, TimeValue::enhanced, "amplified", 2);
    }

    private static Effect createEffect(PotionEffectType type,
                                       Function<TimeValue, Integer> valueFunction,
                                       String propertyName,
                                       int amplifier) {
        Objects.requireNonNull(type, "type");
        if (type.isInstant()) {
            throw new EffectTimeException("Effect type is instant");
        }
        String key = type.getKey().getKey();
        if (!DATA.containsKey(key)) {
            throw new EffectTimeException("Effect type not found (maybe it don't have a time " +
                    "specified or is from a different minecraft version?)");
        }
        TimeValue time = DATA.get(key).times();
        int value = valueFunction.apply(time);
        if (value <= -2 || value == 0) {
            throw new EffectTimeException("Effect type doesn't have property \"" + propertyName + "\" or it is invalid: " + value);
        }
        return new Effect(type.createEffect(value, amplifier));
    }

    @JsonSerialize(using = EffectDataSerializer.class)
    @JsonDeserialize(using = EffectDataDeserializer.class)
    public record EffectData(NamespacedKey namespacedKey, int typeId, TimeValue times, int color,
                             int potionColor, int oldColor, boolean instant, Category category,
                             String description) {

        public boolean isExtendable() {
            return times.isExtendable();
        }

        public boolean isUpgradable() {
            return times.isUpgradable();
        }

    }

    private static class EffectDataSerializer extends JsonSerializer<EffectData> {

        @Override
        public void serialize(EffectData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();

            gen.writeObjectField("namespacedKey", value.namespacedKey().toString());
            gen.writeObjectField("typeId", value.typeId());
            if (value.times() != null) {
                gen.writeObjectField("times", value.times());
            }
            gen.writeStringField("color", Integer.toHexString(value.color()).toUpperCase());
            if (value.potionColor() != -1) {
                gen.writeStringField("potionColor", Integer.toHexString(value.potionColor())
                        .toUpperCase());
            }
            if (value.oldColor() != -1) {
                gen.writeStringField("oldColor", Integer.toHexString(value.oldColor())
                        .toUpperCase());
            }
            gen.writeObjectField("instant", value.instant());
            gen.writeStringField("category", value.category().name());
            gen.writeStringField("description", value.description());

            gen.writeEndObject();
        }

    }

    private static class EffectDataDeserializer extends JsonDeserializer<EffectData> {

        @Override
        public EffectData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            JsonNode node = p.getCodec().readTree(p);

            NamespacedKey namespacedKey = Stardust.key(node.get("namespacedKey").asText());
            int typeId = node.get("typeId").asInt();
            TimeValue times = node.has("times") ? p.getCodec().treeToValue(node.get("times"),
                    TimeValue.class) : null;
            int color = Integer.parseInt(node.get("color").asText(), 16);
            int potionColor = node.has("potionColor") ? Integer.parseInt(node
                    .get("potionColor").asText(), 16) : -1;
            int oldColor = node.has("oldColor") ? Integer.parseInt(node
                    .get("oldColor").asText(), 16) : -1;
            boolean instant = node.get("instant").asBoolean();
            Category category = Category.valueOf(node.get("category").asText());
            String description = node.get("description").asText();

            return new EffectData(namespacedKey, typeId, times, color,
                    potionColor, oldColor, instant, category, description);
        }

    }

    @JsonSerialize(using = TimeValueSerializer.class)
    @JsonDeserialize(using = TimeValueDeserializer.class)
    public record TimeValue(int normal, int extended, int enhanced) {

        public boolean isExtendable() {
            return extended > 0;
        }

        public boolean isUpgradable() {
            return enhanced > 0;
        }

    }

    private static class TimeValueSerializer extends JsonSerializer<TimeValue> {

        @Override
        public void serialize(TimeValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();

            writeIfRegular(value.normal(), "normal", gen);
            writeIfRegular(value.extended(), "extended", gen);
            writeIfRegular(value.enhanced(), "enhanced", gen);

            gen.writeEndObject();
        }

        private void writeIfRegular(int value, String fieldName, JsonGenerator gen) throws IOException {
            if (value == -1 || value > 0) {
                gen.writeObjectField(fieldName, value);
            }
        }

    }

    private static class TimeValueDeserializer extends JsonDeserializer<TimeValue> {

        @Override
        public TimeValue deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            int normal = node.has("normal") ? node.get("normal").asInt() : 0;
            int extended = node.has("extended") ? node.get("extended").asInt() : 0;
            int enhanced = node.has("enhanced") ? node.get("enhanced").asInt() : 0;
            if (normal < 0 || extended < 0 || enhanced < 0) {
                throw new IllegalArgumentException("Negative time field for potion effect in JSON: " + node);
            }
            return new TimeValue(normal, extended, enhanced);
        }

    }

    @StandardException
    public static class EffectTimeException extends RuntimeException {
    }

    static {
        try (InputStream stream = Effect.class.getResourceAsStream("/potion_effects.json")) {
            if (stream == null) {
                throw new NullPointerException("null stream for potion_effects.json");
            }
            ObjectMapper mapper = ObjectMapperFactory.getDefault();
            Map<String, EffectData> data = mapper.readValue(stream, mapper.getTypeFactory()
                    .constructMapType(Map.class, String.class, EffectData.class));
            DATA = Collections.unmodifiableMap(data);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load potion times json", e);
        }
    }

}
