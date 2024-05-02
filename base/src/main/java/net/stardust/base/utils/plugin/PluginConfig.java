package net.stardust.base.utils.plugin;

import net.stardust.base.BasePlugin;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.events.BaseListener;
import net.stardust.base.utils.Throwables;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.Set;

public final class PluginConfig {

    private static final PluginConfig INSTANCE = new PluginConfig();

    private BasePlugin plugin;
    private boolean pluginSet;
    
    private PluginConfig() {}

    public void registerEvents(Listener... listeners) {
        PluginManager manager = plugin.getServer().getPluginManager();
        for(Listener listener : listeners) {
            manager.registerEvents(listener, plugin);
        }
    }
    
    public void registerAllEvents() {
        registerAllEvents(getBaseReflections());
    }
            
    private void registerAllEvents(Reflections reflections) {
        Set<Class<?>> listeners = reflections.getTypesAnnotatedWith(BaseListener.class);
        listeners.stream().filter(Listener.class::isAssignableFrom).forEach(clazz -> {
            try {
                if(clazz == plugin.getClass()) {
                    registerEvents((Listener) plugin);
                } else {
                    registerEvents(instantiate(clazz, Listener.class));
                }
            } catch(Exception e) {
                Throwables.sendAndThrow("plugin-utils/register-all-events/" + plugin.getName(), e);
            }
        });
    }

    public void registerCommand(String name, CommandExecutor executor) {
        plugin.getCommand(name).setExecutor(executor);
    }

    public void registerAllCommands() {
        registerAllCommands(getBaseReflections());
    }

    private void registerAllCommands(Reflections reflections) {
        Set<Class<?>> commands = reflections.getTypesAnnotatedWith(BaseCommand.class);
        commands.stream().filter(CommandExecutor.class::isAssignableFrom).forEach(clazz -> {
            try {
                if(clazz == plugin.getClass()) {
                    throw new RuntimeException("O plugin não pode ser ele mesmo um comando");
                }
                BaseCommand baseCommand = clazz.getAnnotation(BaseCommand.class);
                registerCommand(baseCommand.value(), instantiate(clazz, CommandExecutor.class));
            } catch(Exception e) {
                Throwables.sendAndThrow("plugin-utils/register-all-commands/" + plugin.getName(), e);
            }
        });
    }

    public void registerAll() {
        Reflections reflections = getBaseReflections();
        registerAllEvents(reflections);
        registerAllCommands(reflections);
    }

    public void unregister(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    private Reflections getBaseReflections() {
        return new Reflections(new ConfigurationBuilder()
                .forPackage(plugin.getClass().getPackageName()));
    }

    private <T> T instantiate(Class<?> clazz, Class<T> type) throws Exception {
        Object instance = null;
        for(Constructor<?> constructor : clazz.getConstructors()) {
            switch(constructor.getParameterCount()) {
                case 0 -> {
                    instance = constructor.newInstance();
                }
                case 1 -> {
                    Class<?> parameterType = constructor.getParameterTypes()[0];
                    if(parameterType.isAssignableFrom(plugin.getClass())) {
                        instance = constructor.newInstance(plugin);
                    }
                }
            }
            if(instance != null) {
                break;
            }
        }
        return type.cast(instance);
    }

    public BasePlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(BasePlugin plugin) {
        if(pluginSet) {
            throw new PluginSetException("plugin was already defined");
        }
        this.plugin = Objects.requireNonNull(plugin);
        pluginSet = true;
    }

    public static PluginConfig get() {
        return INSTANCE;
    }

}
