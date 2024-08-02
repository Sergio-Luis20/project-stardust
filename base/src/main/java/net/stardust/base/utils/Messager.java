package net.stardust.base.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.ChatType.Bound;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.stardust.base.BasePlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class Messager {
	
	@NonNull
	private BasePlugin plugin;
	
	public void message(Audience recipient, Component component) {
		message(() -> recipient.sendMessage(component));
	}

	public void message(Audience recipient, Component... messages) {
		message(() -> Arrays.asList(messages).forEach(recipient::sendMessage));
	}

	public void message(Audience recipient, ComponentLike component) {
		message(() -> recipient.sendMessage(component));
	}

	public void message(Audience recipient, SignedMessage signedMessage, Bound bound) {
		message(() -> recipient.sendMessage(signedMessage, bound));
	}

	public void message(Audience recipient, Component component, Bound bound) {
		message(() -> recipient.sendMessage(component, bound));
	}

	public void message(Audience recipient, ComponentLike component, Bound bound) {
		message(() -> recipient.sendMessage(component, bound));
	}

	public void actionBar(Audience recipient, Component component) {
		message(() -> recipient.sendActionBar(component));
	}

	public void actionBar(Audience recipient, ComponentLike component) {
		message(() -> recipient.sendActionBar(component));
	}

	public void message(Collection<? extends Audience> recipients, Component component) {
		message(() -> recipients.forEach(recipient -> recipient.sendMessage(component)));
	}

	public void message(Collection<? extends Audience> recipients, ComponentLike component) {
		message(() -> recipients.forEach(recipient -> recipient.sendMessage(component)));
	}

	public void message(Collection<? extends Audience> recipients, SignedMessage signedMessage, Bound bound) {
		message(() -> recipients.forEach(recipient -> recipient.sendMessage(signedMessage, bound)));
	}

	public void message(Collection<? extends Audience> recipients, Component component, Bound bound) {
		message(() -> recipients.forEach(recipient -> recipient.sendMessage(component, bound)));
	}

	public void message(Collection<? extends Audience> recipients, ComponentLike component, Bound bound) {
		message(() -> recipients.forEach(recipient -> recipient.sendMessage(component, bound)));
	}

	public void actionBar(Collection<? extends Audience> recipients, Component component) {
		message(() -> recipients.forEach(recipient -> recipient.sendActionBar(component)));
	}

	public void actionBar(Collection<? extends Audience> recipients, ComponentLike component) {
		message(() -> recipients.forEach(recipient -> recipient.sendActionBar(component)));
	}
	
	public void message(CommandSender recipient, String message) {
		message(() -> recipient.sendMessage(message));
	}
	
	public void message(CommandSender recipient, String... messages) {
		message(() -> recipient.sendMessage(messages));
	}

	public void message(CommandSender recipient, Component... messages) {
		message(() -> Arrays.asList(messages).forEach(recipient::sendMessage));
	}

	public void plainMessage(CommandSender recipient, String message) {
		message(() -> recipient.sendPlainMessage(message));
	}

	public void richMessage(CommandSender recipient, String message) {
		message(() -> recipient.sendRichMessage(message));
	}

	public void message(Collection<? extends CommandSender> recipients, String message) {
		message(() -> recipients.forEach(recipient -> recipient.sendMessage(message)));
	}

	public void message(Collection<? extends CommandSender> recipients, String... messages) {
		message(() -> recipients.forEach(recipient -> recipient.sendMessage(messages)));
	}

	public void message(Collection<? extends CommandSender> recipients, Component... messages) {
		message(() -> recipients.forEach(recipient -> Arrays.asList(messages).forEach(message -> recipient.sendMessage(message))));
	}
	
	public void message(Messageable messageable, String message) {
		message(() -> messageable.sendMessage(message));
	}

	public void message(Messageable messageable, String... messages) {
		message(() -> messageable.sendMessage(messages));
	}

	public void message(Messageable messageable, Component message) {
		message(() -> messageable.sendMessage(message));
	}

	public void message(Messageable messageable, Component... messages) {
		message(() -> messageable.sendMessage(messages));
	}

	public void plainMessage(Collection<? extends CommandSender> recipients, String message) {
		message(() -> recipients.forEach(recipient -> recipient.sendPlainMessage(message)));
	}

	public void richMessage(Collection<? extends CommandSender> recipients, String message) {
		message(() -> recipients.forEach(recipient -> recipient.sendRichMessage(message)));
	}

	public void rawMessage(Player player, String message) {
		message(() -> player.sendRawMessage(message));
	}

	public void pluginMessage(Player player, Plugin plugin, String channel, byte[] message) {
		message(() -> player.sendPluginMessage(plugin, channel, message));
	}

	public void rawMessage(Collection<? extends Player> players, String message) {
		message(() -> players.forEach(player -> player.sendRawMessage(message)));
	}

	public void pluginMessage(Collection<? extends Player> players, Plugin plugin, String channel, byte[] message) {
		message(() -> players.forEach(player -> player.sendPluginMessage(plugin, channel, message)));
	}

	// ---------------------
	
	public void messageAndWait(Audience recipient, Component component) {
		messageAndWait(() -> recipient.sendMessage(component));
	}

	public void messageAndWait(Audience recipient, Component... messages) {
		messageAndWait(() -> Arrays.asList(messages).forEach(recipient::sendMessage));
	}

	public void messageAndWait(Audience recipient, ComponentLike component) {
		messageAndWait(() -> recipient.sendMessage(component));
	}

	public void messageAndWait(Audience recipient, SignedMessage signedMessage, Bound bound) {
		messageAndWait(() -> recipient.sendMessage(signedMessage, bound));
	}

	public void messageAndWait(Audience recipient, Component component, Bound bound) {
		messageAndWait(() -> recipient.sendMessage(component, bound));
	}

	public void messageAndWait(Audience recipient, ComponentLike component, Bound bound) {
		messageAndWait(() -> recipient.sendMessage(component, bound));
	}

	public void actionBarAndWait(Audience recipient, Component component) {
		messageAndWait(() -> recipient.sendActionBar(component));
	}

	public void actionBarAndWait(Audience recipient, ComponentLike component) {
		messageAndWait(() -> recipient.sendActionBar(component));
	}

	public void messageAndWait(Collection<? extends Audience> recipients, Component component) {
		messageAndWait(() -> recipients.forEach(recipient -> recipient.sendMessage(component)));
	}

	public void messageAndWait(Collection<? extends Audience> recipients, ComponentLike component) {
		messageAndWait(() -> recipients.forEach(recipient -> recipient.sendMessage(component)));
	}

	public void messageAndWait(Collection<? extends Audience> recipients, SignedMessage signedMessage, Bound bound) {
		messageAndWait(() -> recipients.forEach(recipient -> recipient.sendMessage(signedMessage, bound)));
	}

	public void messageAndWait(Collection<? extends Audience> recipients, Component component, Bound bound) {
		messageAndWait(() -> recipients.forEach(recipient -> recipient.sendMessage(component, bound)));
	}

	public void messageAndWait(Collection<? extends Audience> recipients, ComponentLike component, Bound bound) {
		messageAndWait(() -> recipients.forEach(recipient -> recipient.sendMessage(component, bound)));
	}

	public void actionBarAndWait(Collection<? extends Audience> recipients, Component component) {
		messageAndWait(() -> recipients.forEach(recipient -> recipient.sendActionBar(component)));
	}

	public void actionBarAndWait(Collection<? extends Audience> recipients, ComponentLike component) {
		messageAndWait(() -> recipients.forEach(recipient -> recipient.sendActionBar(component)));
	}
	
	public void messageAndWait(CommandSender recipient, String message) {
		messageAndWait(() -> recipient.sendMessage(message));
	}
	
	public void messageAndWait(CommandSender recipient, String... messages) {
		messageAndWait(() -> recipient.sendMessage(messages));
	}

	public void messageAndWait(CommandSender recipient, Component... messages) {
		messageAndWait(() -> Arrays.asList(messages).forEach(recipient::sendMessage));
	}

	public void messageAndWait(Messageable messageable, String message) {
		messageAndWait(() -> messageable.sendMessage(message));
	}

	public void messageAndWait(Messageable messageable, String... messages) {
		messageAndWait(() -> messageable.sendMessage(messages));
	}

	public void messageAndWait(Messageable messageable, Component message) {
		messageAndWait(() -> messageable.sendMessage(message));
	}

	public void messageAndWait(Messageable messageable, Component... messages) {
		messageAndWait(() -> messageable.sendMessage(messages));
	}

	public void plainMessageAndWait(CommandSender recipient, String message) {
		messageAndWait(() -> recipient.sendPlainMessage(message));
	}

	public void richMessageAndWait(CommandSender recipient, String message) {
		messageAndWait(() -> recipient.sendRichMessage(message));
	}

	public void messageAndWait(Collection<? extends CommandSender> recipients, String message) {
		messageAndWait(() -> recipients.forEach(recipient -> recipient.sendMessage(message)));
	}

	public void messageAndWait(Collection<? extends CommandSender> recipients, String... messages) {
		messageAndWait(() -> recipients.forEach(recipient -> recipient.sendMessage(messages)));
	}

	public void messageAndWait(Collection<? extends CommandSender> recipients, Component... messages) {
		messageAndWait(() -> recipients.forEach(recipient -> Arrays.asList(messages).forEach(message -> recipient.sendMessage(message))));
	}

	public void plainMessageAndWait(Collection<? extends CommandSender> recipients, String message) {
		messageAndWait(() -> recipients.forEach(recipient -> recipient.sendPlainMessage(message)));
	}

	public void richMessageAndWait(Collection<? extends CommandSender> recipients, String message) {
		messageAndWait(() -> recipients.forEach(recipient -> recipient.sendRichMessage(message)));
	}
	
	public void rawMessageAndWait(Player player, String message) {
		messageAndWait(() -> player.sendRawMessage(message));
	}

	public void pluginMessageAndWait(Player player, Plugin plugin, String channel, byte[] message) {
		messageAndWait(() -> player.sendPluginMessage(plugin, channel, message));
	}

	public void rawMessageAndWait(Collection<? extends Player> players, String message) {
		messageAndWait(() -> players.forEach(player -> player.sendRawMessage(message)));
	}

	public void pluginMessageAndWait(Collection<? extends Player> players, Plugin plugin, String channel, byte[] message) {
		messageAndWait(() -> players.forEach(player -> player.sendPluginMessage(plugin, channel, message)));
	}

	// ---------------------

	private void message(Runnable runnable) {
		sendMessage(runnable, StardustThreads::run);
	}

	private void messageAndWait(Runnable runnable) {
		sendMessage(runnable, StardustThreads::runAndWait);
	}

	private void sendMessage(Runnable runnable, BiConsumer<BasePlugin, Runnable> biConsumer) {
		if(Bukkit.isPrimaryThread()) {
			runnable.run();
		} else {
			biConsumer.accept(plugin, runnable);
		}
	}
	
}
