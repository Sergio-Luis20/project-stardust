package net.stardust.terrains;

import lombok.Getter;
import net.stardust.base.BasePlugin;
import net.stardust.base.events.DefaultListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TerrainsPlugin extends BasePlugin {

    @Getter
    private static World lobby;

    private List<Player> blockServerMode = new ArrayList<>();

    @Override
    public void onEnable() {
        super.onEnable();
        lobby = Bukkit.getWorld("world");
        getPluginConfig().registerEvents(new DefaultListener(TerrainsPlugin::getLobby));
        if(getConfig().getBoolean("load-terrains-on-startup")) {
            TerrainsService.INSTANCE.loadTerrains();
        }
    }

}
