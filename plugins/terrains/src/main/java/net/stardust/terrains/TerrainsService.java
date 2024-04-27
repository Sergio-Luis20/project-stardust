package net.stardust.terrains;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.FileConfiguration;

import net.stardust.base.utils.BatchList;
import net.stardust.base.utils.plugin.PluginConfig;

public class TerrainsService {

	public static final TerrainsService INSTANCE = new TerrainsService();
	
	private TerrainsPlugin plugin;
	private BatchList<String> terrains;
	private int batchSize;
	
	private TerrainsService() {
		plugin = (TerrainsPlugin) PluginConfig.get().getPlugin();
		batchSize = plugin.getConfig().getInt("terrains-batch-size");
		terrains = BatchList.withBatchSize(batchSize);
	}
	
	public BatchList<World> getTerrains() {
		return terrains.stream().map(Bukkit::getWorld).collect(BatchList.collector(batchSize));
	}

	public BatchList<String> getNamesList() {
		return new BatchList<>(batchSize, terrains);
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
		terrains.add(Bukkit.createWorld(creator).getName());
	}
	
	public void unloadTerrains() {
		for(World terrain : getTerrains()) {
			Bukkit.unloadWorld(terrain, true);
		}
		terrains.clear();
	}
	
	public void unloadTerrain(String worldName) {
		Bukkit.unloadWorld(worldName, true);
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
