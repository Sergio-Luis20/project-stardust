package net.stardust.terrains;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.command.SenderType;
import net.stardust.base.model.terrain.Home;
import net.stardust.base.utils.AutomaticMessages;
import net.stardust.base.utils.BatchList;
import net.stardust.base.utils.persistence.DataManager;

@BaseCommand(value = "home", types = SenderType.PLAYER)
public class HomeCommand extends DirectCommand<TerrainsPlugin> {

    private int limit;
    private String key = "stardust:player_homes";
    private Set<String> allowedWorlds;

    public HomeCommand(TerrainsPlugin plugin) {
        super(plugin);
        FileConfiguration config = plugin.getConfig();
        limit = config.getInt("homes-limit-per-player");
        allowedWorlds = new HashSet<>(config.getStringList("home-allowed-worlds"));
    }

    @CommandEntry
    public void home() {
        home("");
    }

    @CommandEntry
    @SuppressWarnings("unchecked")
    public void home(String name) {
        Player player = sender();
        DataManager<Player> dataManager = new DataManager<>(player);
        Set<Home> homes = dataManager.readObject(key, Set.class);
        if(homes == null) {
            player.sendMessage(miniMessage.deserialize("<red><lang:world.home.dont-have-any>"));
            return;
        }
        Home home = findHome(name, homes);
        if(home == null) {
            player.sendMessage(AutomaticMessages.notFound("word.home"));
            return;
        }
        player.sendMessage(AutomaticMessages.teleportingTo(Component.translatable("world.home.home-arg", 
            NamedTextColor.GOLD, Component.text(name, NamedTextColor.GOLD))));
        player.teleport(home.getLocation());
    }

    @CommandEntry("set")
    public void homeSet() {
        homeSet("");
    }

    @CommandEntry("set")
    @SuppressWarnings("unchecked")
    public void homeSet(String name) {
        Player player = sender();
        World world = player.getWorld();
        if(TerrainsService.INSTANCE.containsTerrain(world)) {


            /*
             * Fazer aqui a lógica para verificar se o jogador está dentro de um terreno,
             * se o terreno é dele e, caso não seja, se tem permissão do dono para criar
             * uma home nele. Se o jogador não está dentro de um terreno, mas ainda assim,
             * como diz a condição do if, está num mundo de terrenos, então ele só poderá
             * criar uma home se estiver dentro de um terreno, seja dele ou de outra pessoa
             * que lhe deu permissão.
             */


        } else if(!allowedWorlds.contains(world.getName())) {
            player.sendRichMessage("<red><lang:world.home.cannot-create-home-in-this-world>");
            return;
        }
        DataManager<Player> dataManager = new DataManager<>(player);
        Set<Home> homes = dataManager.readObject(key, Set.class);
        if(homes == null) {
            homes = new HashSet<>();
        }
        Home home = findHome(name, homes);
        Location pLoc = player.getLocation();
        if(home != null) {
            home.setLocation(pLoc);
            player.sendRichMessage("<green><lang:world.home.changed-to:'<gold>" 
                + name + "':'<light_purple>" + getHomeString(player) + "'>");
        } else if(homes.size() > limit) {
            player.sendRichMessage("<red><lang:world.home.limit-reached>");
            return;
        } else {
            home = new Home(player, name, pLoc);
            homes.add(home);
            player.sendRichMessage("<green><lang:world.home.created-at:'<gold>" + getHomeString(player) + "'>");
        }
        dataManager.writeObject(key, homes);
    }

    @CommandEntry("delete")
    public void deleteHome() {
        deleteHome("");
    }

    @CommandEntry("delete")
    @SuppressWarnings("unchecked")
    public void deleteHome(String name) {
        Player player = sender();
        DataManager<Player> dataManager = new DataManager<>(player);
        Set<Home> homes = dataManager.readObject(key, Set.class);
        if(homes == null) {
            player.sendRichMessage("<red><lang:world.home.dont-have-any>");
            return;
        }
        Home home = findHome(name, homes);
        if(home != null) {
            homes.remove(home);
            dataManager.writeObject(key, homes);
            player.sendRichMessage("<green><lang:world.home.removed:'<gold>" + name + "'");
            return;
        }
        player.sendMessage(AutomaticMessages.notFound("word.home"));
    }

    @CommandEntry("list")
    public void listHomes() {
        listHomes(1);
    }

    @CommandEntry("list")
    @SuppressWarnings("unchecked")
    public void listHomes(int page) {
        Player player = sender();
        int index = page - 1;
        if(index < 0) {
            player.sendMessage(AutomaticMessages.negativePage());
            return;
        }
        DataManager<Player> dataManager = new DataManager<>(player);
        Set<Home> homes = dataManager.readObject(key, Set.class);
        if(homes == null) {
            player.sendRichMessage("<red><lang:world.home.dont-have-any>");
            return;
        }
        final int batchSize = 5;
        BatchList<Home> batchList = new BatchList<>(batchSize, homes);
        List<Home> batch;
        try {
            batch = batchList.getBatch(index);
        } catch(IndexOutOfBoundsException e) {
            player.sendMessage(AutomaticMessages.greaterPage());
            return;
        }
        Component p = Component.text("(p. " + page + "/" + batchList.getTotalBatches() + ")", NamedTextColor.GOLD);
        player.sendMessage(AutomaticMessages.pageable("home-list", p));
        for(Home home : batch) {
            Component prefix = miniMessage.deserialize("<light_purple>» ");
            Component name = miniMessage.deserialize("<gold>" + home.getName() + " ");
            Component homeString = miniMessage.deserialize("<light_purple>[<aqua>" + getHomeString(player, home.getLocation()) + "<light_purple>]");
            player.sendMessage(prefix.append(name).append(homeString));
        }
    }

    private String getHomeString(Player player) {
        return getHomeString(player, player.getLocation());
    }
 
    private String getHomeString(Player player, Location loc) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(player.locale());
        DecimalFormat format = new DecimalFormat("#.#", symbols);
        return loc.getWorld().getName() + "(" + String.join(", ", format.format(loc.getX()), format
            .format(loc.getY()), format.format(loc.getZ())) + ")";
    }

    private Home findHome(String name, Set<Home> homes) {
        for(Home home : homes) {
            if(home.getName().equals(name)) {
                return home;
            }
        }
        return null;
    }

}
