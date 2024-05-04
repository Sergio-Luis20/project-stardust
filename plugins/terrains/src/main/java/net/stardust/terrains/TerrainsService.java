package net.stardust.terrains;

import net.stardust.base.utils.plugin.PluginConfig;
import net.stardust.base.utils.world.WorldUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TerrainsService {

	public static final TerrainsService INSTANCE = new TerrainsService();
	
	private TerrainsPlugin plugin;
	private List<String> terrains;

	private TerrainsService() {
		plugin = (TerrainsPlugin) PluginConfig.get().getPlugin();
		terrains = new ArrayList<>();
	}
	
	public List<World> getTerrains() {
		return terrains.stream().map(Bukkit::getWorld).toList();
	}

	public List<String> getNamesList() {
		return Collections.unmodifiableList(terrains);
	}
	
	public boolean containsTerrain(String worldName) {
		return Collections.binarySearch(terrains, worldName) >= 0;
	}

	public boolean containsTerrain(World world) {
		return containsTerrain(world.getName());
	}
	
	public void loadTerrains() {
		List<String> worldNames = plugin.getConfig().getStringList("terrains");
		for(String worldName : worldNames) {
			loadTerrain0(worldName);
		}
		Collections.sort(terrains);
	}
	
	public void loadTerrain(String worldName) {
		if(!containsTerrain(worldName)) {
			loadTerrain0(worldName);
			FileConfiguration config = plugin.getConfig();
			List<String> worldNames = config.getStringList("terrains");
			if(!worldNames.contains(worldName)) {
				worldNames.add(worldName);
				config.set("terrains", worldNames);
				plugin.saveConfig();
			}
			Collections.sort(terrains);
		}
	}
	
	private void loadTerrain0(String worldName) {
		WorldCreator creator = new WorldCreator(worldName);
		creator.type(WorldType.FLAT);
		creator.generateStructures(false);
		World world = WorldUtils.loadWorld(creator);
		world.setDifficulty(Difficulty.PEACEFUL);
		terrains.add(world.getName());
	}
	
	public void unloadTerrains() {
		for(World terrain : getTerrains()) {
			WorldUtils.unloadWorld(terrain, true);
		}
		terrains.clear();
	}
	
	public void unloadTerrain(String worldName) {
		WorldUtils.unloadWorld(worldName, true);
		int index = Collections.binarySearch(terrains, worldName);
		if(index >= 0) {
			terrains.remove(index);
		}
	}
	
	public void deleteTerrainFromConfig(String worldName) {
		unloadTerrain(worldName);
		plugin.getConfig().set("terrains", terrains);
		plugin.saveConfig();
	}
	
}
