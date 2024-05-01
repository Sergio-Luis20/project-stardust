package net.stardust.base.command;

import br.sergio.utils.Pair;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.stardust.base.BasePlugin;
import net.stardust.base.Communicable;
import net.stardust.base.utils.AutomaticMessages;
import net.stardust.base.utils.Messager;
import net.stardust.base.utils.StardustThreads;
import net.stardust.base.utils.Throwables;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;

@Getter
public abstract sealed class StardustCommand<T extends BasePlugin> implements CommandExecutor, Communicable permits SyncCommand, AsyncCommand, DirectCommand, VirtualCommand {

    protected final T plugin;
	protected final String name;
    protected final Messager messager;
	protected final MiniMessage miniMessage;
	protected final Set<SenderType> senderTypes;
	protected final boolean opOnly;
	protected final Component failMessage;
	
	private Map<Thread, CommandSender> senders;
	private MethodCommandScanner scanner;

	public StardustCommand(T plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
		BaseCommand ann = getClass().getAnnotation(BaseCommand.class);
		this.name = ann.value();
		this.opOnly = ann.opOnly();
		String usageKey = ann.usageKey();
		this.failMessage = usageKey.isEmpty() ? AutomaticMessages.notFound("word.command") :
				Component.translatable(usageKey, NamedTextColor.RED);

		// Types validation
		SenderType[] types = ann.types();
		if(types.length == 0) {
			throw new StardustCommandException("SenderType array is empty");
		}
		for(int i = 0; i < types.length; i++) {
			if(types[i] == null) {
				throw new StardustCommandException("Element of SenderType array is null at index " + i);
			}
		}

		this.senderTypes = Set.of(types);
		messager = plugin.getMessager();
		miniMessage = MiniMessage.miniMessage();
		senders = new ConcurrentHashMap<>();
		scanner = new MethodCommandScanner(getClass());
		scanner.scan();
	}
    
    abstract void execute(Runnable task);
    
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		SenderType type = SenderType.getSenderType(sender.getClass());
		if(canExecute(senderTypes, type) && (!opOnly || sender.isOp())) {
			plugin.getVirtual().execute(() -> {
				Pair<Method, Object[]> result = scanner.find(args);
				if(result != null) {
					Method endpoint = result.getMale();
					CommandEntry entry = endpoint.getAnnotation(CommandEntry.class);
					if(entry.opOnly() && !sender.isOp()) {
						if(entry.showMessage()) {
							messager.message(sender, Component.translatable("command.no-permission", NamedTextColor.RED));
						}
						return;
					}
					Set<SenderType> types = Set.of(entry.types());
					if(!canExecute(types, type)) {
						if(entry.showMessage()) {
							List<String> str = types.stream().map(SenderType::toString).toList();
							Component allowedTypes = Component.text(String.join(", ", str), NamedTextColor.DARK_RED);
							Component message = Component.translatable("command.can-only-be-executed-by", NamedTextColor.RED, allowedTypes);
							messager.message(sender, message);
						}
						return;
					}
					execute(() -> {
						senders.put(Thread.currentThread(), sender);
						try {
							endpoint.invoke(this, result.getFemale());
						} catch(Throwable t) {
							String twbType;
							if(t instanceof RuntimeException) {
								twbType = "Runtime exception";
							} else if(t instanceof Exception) {
								twbType = "Exception";
							} else if(t instanceof Error) {
								twbType = "Error";
							} else {
								twbType = "Throwable";
							}
							String message = twbType + " during execution of command \"" + name + "\"";
							StardustCommandException exc = new StardustCommandException(message, t);
							plugin.getLogger().log(Level.SEVERE, message, Throwables.send(getId(), exc));
						} finally {
							senders.remove(Thread.currentThread());
						}
					});
				} else {
					messager.message(sender, failMessage);
				}
        	});
        	return true;
        }
		sender.sendMessage(Component.translatable("command.no-permission", NamedTextColor.RED));
        return false;
    }

	@SuppressWarnings("unchecked")
	protected final <U extends CommandSender> U sender() {
		return (U) senders.get(Thread.currentThread());
	}
	
	public String name(Player player) {
		return StardustThreads.call(plugin, player::getName);
	}
	
	public UUID uniqueId(Player player) {
		return StardustThreads.call(plugin, player::getUniqueId);
	}
	
	public boolean isOp(CommandSender sender) {
		return StardustThreads.call(plugin, sender::isOp);
	}
	
	public Player player(UUID uniqueId) {
		return StardustThreads.call(plugin, () -> Bukkit.getPlayer(uniqueId));
	}
	
	public Player player(String name) {
		return StardustThreads.call(plugin, () -> Bukkit.getPlayer(name));
	}

	public OfflinePlayer offlinePlayer(UUID uniqueId) {
		return StardustThreads.call(plugin, () -> Bukkit.getOfflinePlayer(uniqueId));
	}

	public OfflinePlayer offlinePlayer(String name) {
		return StardustThreads.call(plugin, () -> Bukkit.getOfflinePlayer(name));
	}

	public <U> U applyPlayer(Player player, Function<Player, U> function) {
		return StardustThreads.call(plugin, () -> function.apply(player));
	}

	public boolean testPlayer(Player player, Predicate<Player> predicate) {
		return StardustThreads.call(plugin, () -> predicate.test(player));
	}

	public void acceptPlayer(Player player, Consumer<Player> consumer) {
		StardustThreads.run(plugin, () -> consumer.accept(player));
	}

	public <U> U applyOfflinePlayer(OfflinePlayer player, Function<OfflinePlayer, U> function) {
		return StardustThreads.call(plugin, () -> function.apply(player));
	}

	public boolean testOfflinePlayer(OfflinePlayer player, Predicate<OfflinePlayer> predicate) {
		return StardustThreads.call(plugin, () -> predicate.test(player));
	}

	public void acceptOfflinePlayer(OfflinePlayer player, Consumer<OfflinePlayer> consumer) {
		StardustThreads.run(plugin, () -> consumer.accept(player));
	}
    
    @Override
    public String getId() {
    	return plugin.getId() + "/" + getClass().getSimpleName();
    }

	public static boolean canExecute(Set<SenderType> types, SenderType type) {
		return types.contains(SenderType.ALL) || types.contains(type);
	}
    
}
