package net.stardust.base.utils;

import java.io.IOException;

import org.bukkit.NamespacedKey;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import net.stardust.base.Stardust;

public final class ObjectMapperFactory {
    
    private ObjectMapperFactory() {}

    public static ObjectMapper getDefault() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
               .withFieldVisibility(Visibility.ANY)
               .withGetterVisibility(Visibility.NONE)
               .withSetterVisibility(Visibility.NONE)
               .withCreatorVisibility(Visibility.PUBLIC_ONLY));
        SimpleModule module = new SimpleModule();
        module.addSerializer(NamespacedKey.class, new NamespacedKeySerializer());
        module.addDeserializer(NamespacedKey.class, new NamespacedKeyDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    public static ObjectMapper yaml() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory()
				.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
				.disable(SerializationFeature.FAIL_ON_SELF_REFERENCES)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(Visibility.ANY)
				.withGetterVisibility(Visibility.NONE)
				.withSetterVisibility(Visibility.NONE)
				.withCreatorVisibility(Visibility.PUBLIC_ONLY));
        return mapper;
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
