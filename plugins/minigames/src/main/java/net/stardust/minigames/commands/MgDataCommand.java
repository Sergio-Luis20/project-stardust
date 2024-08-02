package net.stardust.minigames.commands;

import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.VirtualCommand;
import net.stardust.base.model.minigame.MinigameData;
import net.stardust.base.model.minigame.MinigamePlayer;
import net.stardust.base.model.user.User;
import net.stardust.base.utils.AutomaticMessages;
import net.stardust.base.utils.StardustThreads;
import net.stardust.base.utils.database.crud.MinigameDataCrud;
import net.stardust.base.utils.database.crud.UserCrud;
import net.stardust.base.utils.database.lang.Translation;
import net.stardust.minigames.MinigamesPlugin;

@BaseCommand("mgdata")
public class MgDataCommand extends VirtualCommand<MinigamesPlugin> {

    public MgDataCommand(MinigamesPlugin plugin) {
        super(plugin);
    }

    @CommandEntry(types = Player.class)
    public void data(String minigameName) {
        data(minigameName, (Player) sender());
    }

    @CommandEntry
    public void data(String minigameName, String playerName) {
        data(minigameName, Bukkit.getOfflinePlayer(playerName));
    }

    @CommandEntry
    public void data(String minigameName, int rankPosition) {
        CommandSender sender = sender();
        foundMinigame(sender, minigameName, data -> {
            List<MinigamePlayer> players = data.getMinigamePlayers().values().stream()
                    .sorted(Comparator.reverseOrder()).toList();
            int index = rankPosition - 1;
            if(index >= players.size()) {
                sender.sendMessage(AutomaticMessages.notFound("word.position"));
                return;
            }
            displayData(sender, data, players.get(index), players);
        });
    }

    private void data(String minigameName, OfflinePlayer player) {
        CommandSender sender = sender();
        foundMinigame(sender, minigameName, data -> {
            Map<UUID, MinigamePlayer> minigamePlayers = data.getMinigamePlayers();
            MinigamePlayer minigamePlayer = minigamePlayers.get(player.getUniqueId());
            if(minigamePlayer == null) {
                if(StardustThreads.call(plugin, () -> sender.equals(player))) {
                    messager.message(sender, Component.translatable("minigame.no-data-yet", NamedTextColor.RED));
                } else {
                    messager.message(sender, AutomaticMessages.notFound("word.player"));
                }
                return;
            }
            List<MinigamePlayer> orderedList = minigamePlayers.values().stream()
                    .sorted(Comparator.reverseOrder()).toList();
            displayData(sender, data, minigamePlayer, orderedList);
        });
    }

    private void foundMinigame(CommandSender sender, String minigameName, Consumer<MinigameData> consumer) {
        MinigameDataCrud crud = new MinigameDataCrud();
        List<MinigameData> dataList = crud.getAll();
        for(MinigameData data : dataList) {
            if(data.getMinigameName().equalsIgnoreCase(minigameName)) {
                consumer.accept(data);
                return;
            }
        }
        messager.message(sender, AutomaticMessages.notFound("word.minigame"));
    }

    private void displayData(CommandSender sender, MinigameData data, MinigamePlayer player,
                             List<MinigamePlayer> sortedList) {
        UserCrud userCrud = new UserCrud();
        User user = userCrud.getOrThrow(player.getId());
        String name = user.getName();

        int position = sortedList.indexOf(player) + 1;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Translation.locale(sender));
        char separator = symbols.getDecimalSeparator();
        String ratioString = String.valueOf(player.getRatio()).replace('.', separator);

        Component header = Component.translatable("mg-data-display.header", NamedTextColor.BLUE,
                Component.text(name, NamedTextColor.AQUA), Component.text(data.getMinigameName(),
                        NamedTextColor.DARK_PURPLE));
        Component wins = Component.translatable("mg-data-display.wins", NamedTextColor.GOLD,
                Component.text(player.getWins(), NamedTextColor.YELLOW));
        Component losses = Component.translatable("mg-data-display.losses", NamedTextColor.GOLD,
                Component.text(player.getLosses(), NamedTextColor.YELLOW));
        Component ratio = Component.translatable("mg-data-display.ratio", NamedTextColor.GOLD,
                Component.text(ratioString, NamedTextColor.YELLOW));
        Component totalMatches = Component.translatable("mg-data-display.total-matches",
                NamedTextColor.GOLD, Component.text(player.getTotalMatches(), NamedTextColor.YELLOW));
        Component winsRank = Component.translatable("mg-data-display.wins-rank", NamedTextColor.GOLD,
                Component.text(position, NamedTextColor.YELLOW));

        messager.message(sender, header, wins, losses, ratio, totalMatches, winsRank);
    }

}
