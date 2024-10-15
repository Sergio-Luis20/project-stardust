package net.stardust.base.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import net.stardust.base.Stardust;
import org.bukkit.NamespacedKey;

import java.io.IOException;

public final class ObjectMapperFactory {
    
    private ObjectMapperFactory() {}

    public static ObjectMapper getDefault() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.setVisibility(defaultVisibilityChecker(mapper));
        mapper.registerModule(newDefaultModule());
        return mapper;
    }

    public static ObjectMapper defaultNoFail() {
        ObjectMapper mapper = getDefault();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    public static ObjectMapper yaml() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
				.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
				.disable(SerializationFeature.FAIL_ON_SELF_REFERENCES)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.setVisibility(defaultVisibilityChecker(mapper));
        mapper.registerModule(newDefaultModule());
        return mapper;
    }

    private static SimpleModule newDefaultModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(NamespacedKey.class, new NamespacedKeySerializer());
        module.addDeserializer(NamespacedKey.class, new NamespacedKeyDeserializer());
        return module;
    }

    private static VisibilityChecker<?> defaultVisibilityChecker(ObjectMapper mapper) {
        return mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(Visibility.ANY)
                .withGetterVisibility(Visibility.ANY)
                .withSetterVisibility(Visibility.ANY)
                .withCreatorVisibility(Visibility.PUBLIC_ONLY);
    }

    public static class NamespacedKeySerializer extends JsonSerializer<NamespacedKey> {

        @Override
        public void serialize(NamespacedKey value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.asString());
        }

    }

    public static class NamespacedKeyDeserializer extends JsonDeserializer<NamespacedKey> {

        @Override
        public NamespacedKey deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            JsonNode node = p.getCodec().readTree(p);
            return Stardust.key(node.asText());
        }
        
    }

}
