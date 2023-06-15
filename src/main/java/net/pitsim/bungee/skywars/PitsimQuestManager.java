package net.pitsim.bungee.skywars;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.ProxyRunnable;
import net.pitsim.bungee.objects.PitSimServer;
import net.pitsim.bungee.objects.PluginMessage;
import net.pitsim.bungee.pitsim.PitSimServerManager;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PitsimQuestManager implements Listener {

	public static Map<UUID, Integer> gameMap = new HashMap<>();

	public static void addGame(UUID uuid) {
		ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(uuid);
		if(player == null) {
			gameMap.put(uuid, gameMap.getOrDefault(uuid, 0) + 1);
			return;
		}

		for(PitSimServer pitSimServer : PitSimServerManager.mixedServerList) {
			if(pitSimServer.getServerInfo() == player.getServer().getInfo()) {
				PluginMessage message = new PluginMessage().writeString("SKYWARS PASS QUEST").writeString(uuid.toString());
				message.writeInt(1).addServer(pitSimServer.getServerInfo()).send();
				return;
			}
		}

		gameMap.put(uuid, gameMap.getOrDefault(uuid, 0) + 1);
	}

	@EventHandler
	public void onSwitch(ServerConnectedEvent event) {
		ProxiedPlayer player = event.getPlayer();

		if(!gameMap.containsKey(player.getUniqueId())) return;

		ServerInfo serverInfo = event.getServer().getInfo();
		for(PitSimServer pitSimServer : PitSimServerManager.mixedServerList) {
			if(pitSimServer.getServerInfo() == serverInfo) {
				((ProxyRunnable) () -> {
					PluginMessage message = new PluginMessage().writeString("SKYWARS PASS QUEST").writeString(player.getUniqueId().toString())
							.writeInt(gameMap.get(player.getUniqueId())).addServer(pitSimServer.getServerInfo()).send();
					gameMap.remove(player.getUniqueId());
				}).runAfter(1, TimeUnit.SECONDS);
			}
		}
	}

	public static void parseString(String args) {
		if(args.isEmpty()) return;
		String[] players = args.split(",");
		for(String player : players) {
			addGame(UUID.fromString(player));
		}
	}
}
