package net.stardust.generalcmd;

import static net.kyori.adventure.text.Component.translatable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.SenderType;
import net.stardust.base.command.VirtualCommand;
import net.stardust.base.model.economy.wallet.Currency;
import net.stardust.base.model.economy.wallet.Money;
import net.stardust.base.model.economy.wallet.PlayerWallet;
import net.stardust.base.model.user.User;
import net.stardust.base.utils.AutomaticMessages;
import net.stardust.base.utils.StardustThreads;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.database.crud.PlayerWalletCrud;
import net.stardust.base.utils.database.crud.UserCrud;
import net.stardust.base.utils.database.lang.Translation;

@BaseCommand("money")
public class MoneyCommand extends VirtualCommand<GeneralCommandsPlugin> {
	
	private UserCrud userCrud;
	private PlayerWalletCrud walletCrud;
    
    public MoneyCommand(GeneralCommandsPlugin plugin) {
		super(plugin);
		userCrud = new UserCrud();
		walletCrud = new PlayerWalletCrud();
	}
    
	@CommandEntry(types = SenderType.PLAYER)
	public void getSubCommand() {
		Player sender = sender();
		executeGet(sender, name(sender), walletCrud.getOrThrow(uniqueId(sender)));
	}

	@CommandEntry(oneWordFinalString = true)
	public void getSubCommand(String name) {
		getByName(name);
	}

	@CommandEntry(value = "get", oneWordFinalString = true)
	public void get0(String name) {
		getByName(name);
	}

	@CommandEntry("help")
	public void help() {
		CommandSender sender = sender();
        int range = isOp(sender) ? 8 : 7;
        Component[] messages = new Component[range];
        for(int i = 0; i < range; i++) {
        	messages[i] = Translation.get(sender, "money.help.m" + i);
        }
        messager.message(sender, messages);
	}

	@CommandEntry(value = "rank", types = SenderType.PLAYER)
	public void rank() {
		rank(name(sender()));
	}

	@CommandEntry("rank")
	public void rank(int pos) {
		CommandSender sender = sender();
    	List<PlayerWallet> wallets = walletCrud.getAll();
    	List<PlayerWallet> bronzeRank = getSortedList(wallets, PlayerWallet::getBronze);
    	List<PlayerWallet> silverRank = getSortedList(wallets, PlayerWallet::getSilver);
    	List<PlayerWallet> goldRank = getSortedList(wallets, PlayerWallet::getGold);
    	Component[] messages = new Component[4];
    	int index = pos - 1;
		Component dash = Component.text('-');
		Component posComp = translatable("money.rank.pos", NamedTextColor.AQUA, Component.text(pos, NamedTextColor.GREEN));
    	if(index >= wallets.size()) {
    		messages[0] = posComp;
    		messages[1] = translatable("money.rank.bronze-pos", NamedTextColor.GOLD, dash.color(NamedTextColor.GOLD));
    		messages[2] = translatable("money.rank.silver-pos", NamedTextColor.GRAY, dash.color(NamedTextColor.GRAY));
    		messages[3] = translatable("money.rank.gold-pos", NamedTextColor.YELLOW, dash.color(NamedTextColor.YELLOW));
    		messager.message(sender, messages);
    		return;
    	}
    	
    	UUID bronzeId = bronzeRank.get(index).getId();
    	UUID silverId = silverRank.get(index).getId();
    	UUID goldId = goldRank.get(index).getId();
    	
    	List<User> users = userCrud.getAll();
    	users.sort((u1, u2) -> u1.getId().compareTo(u2.getId()));
    	List<UUID> uuids = users.stream().map(User::getId).toList();
    	
    	ExecutorService executorService = plugin.getCached();
    	Future<String> bronzePlayer = executorService.submit(new UserFinder(users, uuids, bronzeId));
    	Future<String> silverPlayer = executorService.submit(new UserFinder(users, uuids, silverId));
    	Future<String> goldPlayer = executorService.submit(new UserFinder(users, uuids, goldId));
    	
		try {
			messages[0] = posComp;
			messages[1] = translatable("money.rank.bronze-pos", NamedTextColor.GOLD, Component.text(bronzePlayer.get(), NamedTextColor.GOLD));
			messages[2] = translatable("money.rank.silver-pos", NamedTextColor.GRAY, Component.text(silverPlayer.get(), NamedTextColor.GRAY));
			messages[3] = translatable("money.rank.gold-pos", NamedTextColor.YELLOW, Component.text(goldPlayer.get(), NamedTextColor.YELLOW));
	    	messager.message(sender, messages);
		} catch (InterruptedException | ExecutionException e) {
			Throwables.send(e);
		}
	}

	@CommandEntry("rank")
	public void rank(String name) {
		CommandSender sender = sender();
		User target = userCrud.byNameOrNull(name);
    	if(target == null) {
    		messager.message(sender, translatable("not-found", NamedTextColor.RED, Component.translatable("word.player", NamedTextColor.RED)));
    		return;
    	}

		PlayerWallet wallet = walletCrud.getOrThrow(target.getId());

		List<PlayerWallet> wallets = walletCrud.getAll();
    	List<PlayerWallet> bronzeRank = getSortedList(wallets, PlayerWallet::getBronze);
    	List<PlayerWallet> silverRank = getSortedList(wallets, PlayerWallet::getSilver);
    	List<PlayerWallet> goldRank = getSortedList(wallets, PlayerWallet::getGold);

    	Component[] messages = new Component[4];
    	messages[0] = translatable("money.rank.wallet", NamedTextColor.AQUA, Component
			.text(target.getName(), NamedTextColor.GREEN));
    	messages[1] = translatable("money.rank.bronze-pos", NamedTextColor.GOLD, Component.text(Collections
			.binarySearch(bronzeRank, wallet, getComparator(PlayerWallet::getBronze)) + 1, NamedTextColor.GOLD));
    	messages[2] = translatable("money.rank.silver-pos", NamedTextColor.GRAY, Component.text(Collections
			.binarySearch(silverRank, wallet, getComparator(PlayerWallet::getSilver)) + 1, NamedTextColor.GRAY));
    	messages[3] = translatable("money.rank.gold-pos", NamedTextColor.YELLOW, Component.text(Collections
			.binarySearch(goldRank, wallet, getComparator(PlayerWallet::getGold)) + 1, NamedTextColor.YELLOW));
    	messager.message(sender, messages);
	}

	@CommandEntry(value = "set", opOnly = true, oneWordFinalString = true)
	public void set(String name, String moneyString) {
		CommandSender sender = sender();
		User target = userCrud.byNameOrNull(name);
    	if(target == null) {
    		messager.message(sender, "Usuário não encontrado");
    		return;
    	}
		Money money = null;
		try {
			money = Money.valueOf(moneyString);
		} catch(IllegalArgumentException e) {
			messager.message(sender, Component.translatable("money.format", NamedTextColor.RED));
			return;
		}
		Currency currency = money.getCurrency();
		BigInteger value = money.getValue();
    	if(walletCrud.updateMoney(target.getId(), new Money(currency, value))) {
    		messager.message(sender, "§a» Atualizada a quantidade de moedas de " + currency
				.getColor() + currency.getConsoleName().toLowerCase() + " §ade §b" + target
				.getName() + " §apara " + currency.getColor() + value);
    		return;
    	}
    	messager.message(sender, "§c» Não foi possível atualizar o dinheiro de " + target.getName());
	}

	@CommandEntry(value = "pay", types = SenderType.PLAYER, oneWordFinalString = true)
	public void pay(String name, String moneyString) {
		Player sender = sender();
		User target = userCrud.byNameOrNull(name);
    	if(target == null) {
    		messager.message(sender, AutomaticMessages.notFound("word.player"));
    		return;
    	}
    	UUID senderId = StardustThreads.call(plugin, () -> sender.getUniqueId());
		if(senderId.equals(target.getId())) {
			messager.message(sender, Component.translatable("money.cannot-pay-yourself", NamedTextColor.RED));
			return;
		}
		Money money = null;
		try {
			money = Money.valueOf(moneyString);
		} catch(IllegalArgumentException e) {
			messager.message(sender, Component.translatable("money.format", NamedTextColor.RED));
			return;
		}
		Currency currency = money.getCurrency();
		BigInteger value = money.getValue();
    	PlayerWallet senderWallet = walletCrud.getOrThrow(senderId);
    	PlayerWallet targetWallet = walletCrud.getOrThrow(target.getId());
    	Money senderTotal = senderWallet.getMoney(currency);
    	if(senderTotal.isSubtractionPossible(value)) {
    		senderTotal.subtract(value);
    		targetWallet.getMoney(currency).add(value);
    		walletCrud.updateAll(new ArrayList<>(Arrays.asList(senderWallet, targetWallet)));
    		// Send messages to players
			Component paidComp = money.toComponent();
    		messager.message(sender, translatable("money.sent", NamedTextColor.GREEN, paidComp, 
				Component.text(target.getName(), NamedTextColor.AQUA)));
    		Player targetPlayer = player(target.getId());
    		if(targetPlayer != null) {
    			// Target is online
    			messager.message(targetPlayer, translatable("money.received", NamedTextColor.GREEN, paidComp, 
					Component.text(sender.getName(), NamedTextColor.AQUA)));
    		}
    		return;
    	}
		sender.sendMessage(translatable("money.dont-have-enough", NamedTextColor.RED));
	}
	
	private void getByName(String name) {
		CommandSender sender = sender();
		User target = userCrud.byNameOrNull(name);
		if(target == null) {
			messager.message(sender,AutomaticMessages.notFound("word.player"));
			return;
		}
		executeGet(sender, target.getName(), walletCrud.getOrThrow(target.getId()));
	}
	
	private void executeGet(CommandSender sender, String name, PlayerWallet wallet) {
        Component[] messages = new Component[4];
        messages[0] = translatable("money.value.header", NamedTextColor.AQUA, Component
			.text(name, NamedTextColor.GREEN));
        messages[1] = translatable("money.value.bronze", NamedTextColor.GOLD, Component
			.text(wallet.getBronze().getValue().toString(), NamedTextColor.GOLD));
        messages[2] = translatable("money.value.silver", NamedTextColor.GRAY, Component
			.text(wallet.getSilver().getValue().toString(), NamedTextColor.GRAY));
        messages[3] = translatable("money.value.gold", NamedTextColor.YELLOW, Component
			.text(wallet.getGold().getValue().toString(), NamedTextColor.YELLOW));
		messager.message(sender, messages);
	}

	private Comparator<PlayerWallet> getComparator(Function<PlayerWallet, Money> function) {
		return Comparator.comparing(function).reversed();
	}
	
	private List<PlayerWallet> getSortedList(List<PlayerWallet> original, Function<PlayerWallet, Money> function) {
        return original.stream().sorted(getComparator(function)).toList();
    }
    
    @AllArgsConstructor
    private class UserFinder implements Callable<String> {
    	
    	private List<User> users;
    	private List<UUID> uuids;
    	private UUID wanted;
    	
		@Override
		public String call() throws Exception {
			return users.get(Collections.binarySearch(uuids, wanted)).getName();
		}
    	
    }
    
}
