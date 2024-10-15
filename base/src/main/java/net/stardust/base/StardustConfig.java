package net.stardust.base;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StardustConfig extends YamlConfiguration {

    private static final char VARIABLE_KEY_VALUE_SEPARATOR = '=';

    private FileConfiguration config;
    private Map<String, String> variables;

    public StardustConfig(URL envVarURL) throws IOException {
        this(null, envVarURL);
    }

    public StardustConfig(FileConfiguration config, URL envVarURL) throws IOException {
        this.config = config;
        Pattern pattern = Pattern.compile("\\A[A-Z][A-Z0-9_]*\\z");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(envVarURL.openStream()))) {
            variables = new HashMap<>();
            int lineCount = 0;
            for (String line; (line = reader.readLine()) != null;) {
                lineCount++;
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                int separatorIndex = line.indexOf(VARIABLE_KEY_VALUE_SEPARATOR);
                if (separatorIndex == -1) {
                    throw new IOException(("Corrupted file format: separator %s not found at line " +
                            "%s.").formatted(VARIABLE_KEY_VALUE_SEPARATOR, lineCount));
                }
                String key = line.substring(0, separatorIndex);
                if (!pattern.matcher(key).matches()) {
                    throw new IOException(("Corrupted file format: key names must be %s, but an invalid " +
                            "name (%s) was found in line %s").formatted(pattern.pattern(), key, lineCount));
                }
                if (variables.containsKey(key)) {
                    throw new IOException(("Corrupted file format: duplicate key (%s) found in " +
                            "line %s").formatted(key, lineCount));
                }
                String value = line.substring(separatorIndex + 1);
                variables.put(key, value);
            }
        }
        YamlConfiguration a;
    }

    public Object getOriginalValue(String path) {
        return super.get(path);
    }

    public Object getOriginalValue(String path, Object def) {
        return super.get(path, def);
    }

    @Override
    public Object get(String path, Object def) {
        return config != null ? config.get(path, def) : super.get(path, def);
    }

    @Override
    public void set(String path, Object value) {
        super.set(path, value);
        if (config != null) {
            config.set(path, value);
        }
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        Pattern pattern = Pattern.compile("\\$\\{([a-zA-Z][a-zA-Z0-9_]*)}");
        StringBuilder builder = new StringBuilder();
        Matcher matcher = pattern.matcher(contents);
        while (matcher.find()) {
            String key = matcher.group(1);
            if (!variables.containsKey(key)) {
                continue;
            }
            matcher.appendReplacement(builder, variables.get(key));
        }
        matcher.appendTail(builder);
        if (config != null) {
            super.loadFromString(contents);
            config.loadFromString(builder.toString());
        } else {
            super.loadFromString(builder.toString());
        }
    }

}
