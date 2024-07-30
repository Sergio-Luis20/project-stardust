package net.stardust.base.model.channel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

public final class ChannelPropertiesProvider {
    
    private ChannelPropertiesProvider() {}

    public static Map<String, Set<String>> getProperties() {
        Map<String, Set<String>> properties = new HashMap<>();
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(Channel.class.getPackageName()));
        reflections.getTypesAnnotatedWith(ChannelProperties.class).forEach(clazz -> {
            ChannelProperties ann = clazz.getAnnotation(ChannelProperties.class);
            Set<String> props = new HashSet<>(Arrays.asList(ann.value()));
            properties.put(clazz.getName(), props);
        });
        return properties;
    }

}
