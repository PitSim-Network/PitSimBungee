package net.pitsim.bungee;

import net.pitsim.bungee.misc.AOutput;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandBlocker implements Listener {

	public static final int DEFAULT_COOLDOWN_SECONDS = 3;

	public static List<UUID> blockedPlayers = new ArrayList<>();
	public static Map<UUID, Integer> commandMap = new HashMap<>();

	public static List<String> blockedCommandList = new ArrayList<>();

	static {
		((ProxyRunnable) () -> commandMap.clear()).runAfterEvery(1, 1, TimeUnit.SECONDS);

		blockedCommandList.add("g ");
		blockedCommandList.add("guild");
	}

	public static void blockPlayer(UUID uuid) {
		blockPlayer(uuid, DEFAULT_COOLDOWN_SECONDS, TimeUnit.SECONDS);
	}

	public static void blockPlayer(UUID uuid, int time, TimeUnit timeUnit) {
		blockedPlayers.add(uuid);
		((ProxyRunnable) () -> blockedPlayers.remove(uuid)).runAfter(time, timeUnit);
	}

	@EventHandler
	public void onCommand(ChatEvent event) {
		if(!event.isCommand()) return;
		if(!(event.getSender() instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();

		commandMap.put(player.getUniqueId(), commandMap.getOrDefault(player.getUniqueId(), 0) + 1);
		if(commandMap.get(player.getUniqueId()) >= 5) player.disconnect(TextComponent.fromLegacyText(ChatColor.RED + "You are sending command too fast!"));

		if(!blockedPlayers.contains(player.getUniqueId())) return;

		for(String s : blockedCommandList) {
			if(event.getMessage().toLowerCase().startsWith("/" + s)) {
				event.setCancelled(true);
				AOutput.color(player, "&cYou may not use that command at this time.");
				return;
			}
		}
	}

	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent event) {
		commandMap.remove(event.getPlayer().getUniqueId());
	}



}
