package net.stardust.base.utils.database.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.kyori.adventure.util.UTF8ResourceBundleControl;

public class YamlResourceBundleControl extends ResourceBundle.Control {

    public static final YamlResourceBundleControl INSTANCE = new YamlResourceBundleControl();

    private List<String> formats;

    private YamlResourceBundleControl() {
        formats = List.of("java.class", "java.properties", "java.yml");
    }
    
    @Override
    public List<String> getFormats(String baseName) {
        if(baseName == null) {
            throw new NullPointerException();
        }
        return formats;
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        if("java.yml".equals(format)) {
            String bundle = toBundleName(baseName, locale);
            String resource = toResourceName(bundle, "yml");
            InputStream stream = null;
            if(reload) {
                URL url = loader.getResource(resource);
                if(url != null) {
                    URLConnection connection = url.openConnection();
                    if(connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resource);
            }
            if(stream != null) {
                // Stream is closed inside loadConfiguration
                BufferedReader bufferedStream = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                FileConfiguration config = YamlConfiguration.loadConfiguration(bufferedStream);
                Set<String> keys = config.getKeys(true);
                StringBuilder builder = new StringBuilder();
                int index = 0, size = keys.size();
                for(String key : keys) {
                    builder.append(key + "=" + config.getString(key));
                    if(index++ < size - 1) {
                        builder.append("\n");
                    }
                }
                StringReader reader = new StringReader(builder.toString());
                return new PropertyResourceBundle(reader);
            }
            return null;
        } else if(formats.contains(format)) {
            return UTF8ResourceBundleControl.get().newBundle(baseName, locale, format, loader, reload);
        } else {
            return super.newBundle(baseName, locale, format, loader, reload);
        }
    }

    public static YamlResourceBundleControl get() {
        return INSTANCE;
    }

}
