package net.stardust.terrains;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import lombok.Getter;
import net.stardust.base.BasePlugin;

@Getter
public class TerrainsPlugin extends BasePlugin {
	
    private List<Player> blockServerMode = new ArrayList<>();

    @Override
    public void onEnable() {
        super.onEnable();
        if(getConfig().getBoolean("load-terrains-on-startup")) {
            TerrainsService.INSTANCE.loadTerrains();
        }
    }

}
