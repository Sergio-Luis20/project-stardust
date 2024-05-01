package net.stardust.base.events;

import lombok.Getter;
import net.stardust.base.Communicable;
import net.stardust.base.utils.Throwables;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;

import java.lang.reflect.Method;
import java.util.*;

public class WorldListener implements Listener, Communicable {
    
    @Getter
    private Listener child;

    @Getter
    private String worldName;
    private World world;

    private Map<String, List<Method>> childMethods;
    
    public WorldListener(Listener child, String worldName) {
        this.child = Objects.requireNonNull(child, "child");
        childMethods = new HashMap<>();
        this.worldName = Objects.requireNonNull(worldName, "worldName").trim();
        if(this.worldName.isEmpty()) {
            throw new InvalidEventClassException("Invalid world name: blank");
        }
        populateChildMethods();
    }

    private void populateChildMethods() {
        Class<?> childClass = child.getClass();
        String className = childClass.getName();
        for(Method method : childClass.getDeclaredMethods()) {
            if(!method.isAnnotationPresent(EventHandler.class)) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if(params.length != 1) {
                throw new InvalidEventClassException(className + " has a method annotated with EventHandler that has more than 1 parameter");
            }
            Class<?> param = params[0];
            if(!Event.class.isAssignableFrom(param)) {
                throw new InvalidEventClassException(className + " has a method annotated with EventHandler with a parameter that's not a subclass of " + Event.class.getName());
            }
            String name = param.getName();
            childMethods.computeIfAbsent(name, n -> new ArrayList<>());
            childMethods.get(name).add(method);
        }
    }

    @EventHandler
    public void onEvent(Event event) {
        List<Method> methods = childMethods.get(event.getClass().getName());
        if(methods != null) {
            World world = getWorld();
            if(world != null && world.equals(getEventWorld(event))) {
                try {
                    for(Method method : methods) {
                        method.invoke(child, event);
                    }
                } catch(Exception e) {
                    Throwables.sendAndThrow(worldName, e);
                }
            }
        }
    }

    private World getEventWorld(Event event) {
        if(event instanceof PlayerEvent playerEvent) {
            return playerEvent.getPlayer().getWorld();
        }
        if(event instanceof EntityEvent entityEvent) {
            return entityEvent.getEntity().getWorld();
        }
        if(event instanceof BlockEvent blockEvent) {
            return blockEvent.getBlock().getWorld();
        }
        if(event instanceof WorldEvent worldEvent) {
            return worldEvent.getWorld();
        }
        if(event instanceof WeatherEvent weatherEvent) {
            return weatherEvent.getWorld();
        }
        return null;
    }

    private World getWorld() {
        if(world == null) {
            world = Bukkit.getWorld(worldName);
        }
        return world;
    }

    @Override
    public String getId() {
        String id = getClass().getSimpleName();
        if(child instanceof Communicable com) {
            return id + "/" + com.getId();
        }
        return id;
    }

}
