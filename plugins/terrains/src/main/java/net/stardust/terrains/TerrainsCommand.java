package net.stardust.terrains;

import java.util.List;

import org.bukkit.entity.Player;

import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.utils.BatchList;

@BaseCommand("terrains")
public class TerrainsCommand extends DirectCommand<TerrainsPlugin> {

    private TerrainsService service = TerrainsService.INSTANCE;

    public TerrainsCommand(TerrainsPlugin plugin) {
        super(plugin);
    }

    @CommandEntry("list")
    public void list() {
        // Aqui mostrar-se-á ao jogador seus próprios terrenos
    }

    @CommandEntry(value = "list server", opOnly = true)
    public void listServer() {
        listTerrains(1);
    }
    
    @CommandEntry(value = "list server", opOnly = true)
    public void listServer(int page) {
        listTerrains(page);
    }

    @CommandEntry(value = "load", opOnly = true)
    public void load(String terrain) {
        Player player = sender();
        player.sendMessage("§e» Tentando carregar o terreno §6" + terrain + " §ecaso ele esteja descarregado...");
        service.loadTerrain(terrain);
        player.sendMessage("§e» Terreno §6" + terrain + " §ecarregado");
    }

    @CommandEntry(value = "unload", opOnly = true)
    public void unload(String terrain) {
        Player player = sender();
        player.sendMessage("§e» Tentando descarregar o terreno §6" + terrain + " §ecaso ele esteja carregado...");
        service.unloadTerrain(terrain);
        player.sendMessage("§e» Terreno §6" + terrain + " §edescarregado");
    }

    @CommandEntry(value = "exists", opOnly = true)
    public void exists(String terrain) {
        Player player = sender();
        if(service.containsTerrain(terrain)) {
            player.sendMessage("§e» O terreno §6" + terrain + " §eexiste");
        } else {
            player.sendMessage("§e» O terreno §6" + terrain + " §enão existe ou está descarregado");
        }
    }

    @CommandEntry(value = "set startup", opOnly = true)
    public void setStartup(boolean startup) {
        plugin.getConfig().set("load-terrains-on-startup", startup);
        plugin.saveConfig();
        sender().sendMessage("§e» Propriedade §2load-terrains-on-startup §edefinida para " + (startup ? "§a" : "§c") + startup);
    }

    @CommandEntry(value = "get startup", opOnly = true)
    public void getStartup() {
        boolean startup = plugin.getConfig().getBoolean("load-terrains-on-startup");
        sender().sendMessage("§e» Propriedade §2load-terrains-on-startup §eestá atualmente com valor " + (startup ? "§a" : "§c") + startup);
    }
    
    private void listTerrains(int page) {
        int index = page - 1;
        Player player = sender();
        if(index < 0) {
            player.sendMessage("§c» Página negativa");
            return;
        }
        BatchList<String> batchList = service.getNamesList();
        List<String> batch;
        try {
            batch = batchList.getBatch(index);
        } catch(IndexOutOfBoundsException e) {
            player.sendMessage("§c» Página maior que a quantidade máxima existente");
            return;
        }
        player.sendMessage("§6» Lista de terrenos atualmente carregados (p. %s/%s):".formatted(page, batchList.getTotalBatches()));
        for(String terrain : batch) {
            player.sendMessage("§a§o» " + terrain);
        }
    }
    
}
