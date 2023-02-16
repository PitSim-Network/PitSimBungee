package dev.wiji.instancemanager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommandBlocker implements Listener {

	public static Map<UUID, Integer> commandMap = new HashMap<>();

	static {
		((ProxyRunnable) () -> commandMap.clear()).runAfterEvery(1, 1, TimeUnit.SECONDS);
	}

	@EventHandler
	public void onCommand(ChatEvent event) {
		if(!event.isCommand()) return;
		if(!(event.getSender() instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();

		commandMap.put(player.getUniqueId(), commandMap.getOrDefault(player.getUniqueId(), 0) + 1);
		if(commandMap.get(player.getUniqueId()) >= 5) player.disconnect(TextComponent.fromLegacyText(ChatColor.RED + "You are sending messages too fast!"));
	}

}
