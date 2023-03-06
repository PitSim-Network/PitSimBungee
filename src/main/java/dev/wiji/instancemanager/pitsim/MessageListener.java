package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.discord.AuctionAlerts;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.market.MarketManager;
import dev.wiji.instancemanager.objects.*;
import dev.wiji.instancemanager.storage.EditSessionManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.UUID;

public class MessageListener implements Listener {

	@EventHandler
	public void onMessage(MessageEvent event) {

		PluginMessage message = event.getMessage();
		List<String> strings = event.getMessage().getStrings();
		List<Integer> integers = event.getMessage().getIntegers();
		List<Boolean> booleans = event.getMessage().getBooleans();

		if(strings.size() >= 2 && strings.get(0).equals("INITIATE STARTUP")) {
			String serverName = strings.get(1);
			for(OverworldServer server : OverworldServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					if(OverworldServerManager.networkIsShuttingDown || server.status.isShuttingDown()) {
						server.hardShutDown();
					} else {
						System.out.println("Server " + serverName + " is now running!");

						BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&2&lKEEPER! &7Server &e" + serverName + " &7is now available!"));
						for(ProxiedPlayer player : BungeeMain.INSTANCE.getProxy().getPlayers()) {
							if(player.getServer().getInfo().getName().contains("pitsim")) {
								player.sendMessage(components);
							}
						}

						if(server.status != ServerStatus.SUSPENDED) server.status = ServerStatus.RUNNING;
						server.setStartTime(System.currentTimeMillis());
						break;
					}
				}
			}

			for(DarkzoneServer server : DarkzoneServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					if(DarkzoneServerManager.networkIsShuttingDown || server.status.isShuttingDown()) {
						server.hardShutDown();
					} else {
						System.out.println("Server " + serverName + " is now running!");
						server.status = ServerStatus.RUNNING;
						server.setStartTime(System.currentTimeMillis());
						MarketManager.updateAll();
						break;
					}
				}
			}

		}

		if(strings.size() >= 2 && strings.get(0).equals("INITIATE FINAL SHUTDOWN")) {
			String serverName = strings.get(1);
			for(OverworldServer server : OverworldServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					server.serverData = null;
					server.status = ServerStatus.SHUTTING_DOWN_FINAL;
					server.staffOverride = false;
					server.beginStartCooldown();
				}
			}

			for(DarkzoneServer server : DarkzoneServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					server.serverData = null;
					server.status = ServerStatus.SHUTTING_DOWN_FINAL;
					server.staffOverride = false;
					server.beginStartCooldown();
				}
			}

		}

		if(strings.size() >= 2 && strings.get(0).equals("INITIATE FINAL RESTART")) {
			String serverName = strings.get(1);
			for(OverworldServer server : OverworldServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					server.serverData = null;
					server.status = ServerStatus.RESTARTING_FINAL;
					server.beginStartCooldown();
				}
			}

			for(DarkzoneServer server : DarkzoneServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					server.serverData = null;
					server.status = ServerStatus.RESTARTING_FINAL;
					server.beginStartCooldown();
				}
			}

		}

		if(strings.size() >= 3 && strings.get(0).equals("STATUS REPORT")) {
			String serverName = strings.get(1);
			String status = strings.get(2);
			for(OverworldServer server : OverworldServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					server.status = ServerStatus.valueOf(status);
					break;
				}
			}

			for(DarkzoneServer server : DarkzoneServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					server.status = ServerStatus.valueOf(status);
					break;
				}
			}

		}


		if(strings.size() >= 2 && strings.get(0).equals("QUEUE")) {

			String playerString = strings.get(1);
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerString);
			if(player == null) return;

			int requested = 0;
			if(integers.size() >= 1) {
				requested = integers.get(0);
			}

			boolean fromDarkzone = false;
			if(booleans.size() >= 1) {
				fromDarkzone = booleans.get(0);
			}

			OverworldServerManager.queueFallback(player, requested, fromDarkzone);
		}

		if(strings.size() >= 2 && strings.get(0).equals("QUEUE DARKZONE")) {

			String playerString = strings.get(1);
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerString);
			if(player == null) return;

			int requested = 0;
			if(integers.size() >= 1) {
				requested = integers.get(0);
			}

			DarkzoneServerManager.queueFallback(player, requested);
		}

		if(strings.size() >= 3 && strings.get(0).equals("BOOSTER USE")) {
			String boosterName = strings.get(1);
			String announcement = strings.get(2);
			int time = integers.get(0);

			PluginMessage outgoingMessage = new PluginMessage();
			outgoingMessage.writeString("BOOSTER USE").writeString(boosterName).writeString(announcement).writeInt(time);
			for(MainGamemodeServer pitSimServer : MainGamemodeServer.serverList) {
				if(pitSimServer.status.isOnline()) message.addServer(pitSimServer.getServerInfo());
			}

			message.send();
		}

		if(strings.size() >= 3 && strings.get(0).equals("EDIT PLAYER")) {
			UUID staffUUID = UUID.fromString(strings.get(1));
			String playerName = strings.get(2);
			EditSessionManager.createSession(staffUUID, playerName);
		}

		if(strings.size() >= 3 && strings.get(0).equals("TELEPORT JOIN")) {
			UUID staffUUID = UUID.fromString(strings.get(1));
			String playerName = strings.get(2);

			int serverIndex = integers.get(0);
			boolean darkzone = booleans.get(0);

			PluginMessage response = new PluginMessage().writeString("TELEPORT JOIN");
			response.writeString(staffUUID.toString()).writeString(playerName);

			ServerInfo serverInfo = MainGamemodeServer.getServer(serverIndex + 1, darkzone).getServerInfo();
			response.addServer(serverInfo).send();
		}

		if(strings.size() >= 3 && strings.get(0).equals("AUCTION ITEM REQUEST")) {
			String playerName = strings.get(1);
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerName);
			ServerInfo serverInfo = BungeeMain.INSTANCE.getProxy().getServerInfo(strings.get(2));

			BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&'
					, "&5&lDARK AUCTION! &e" + playerName + " &7won " + strings.get(3) + " &7for &f" +
							integers.get(2) + " Souls&7."));

			boolean isOnline = false;

			for(OverworldServer overworldServer : OverworldServerManager.serverList) {
				for(ProxiedPlayer pitSimServerPlayer : overworldServer.getPlayers()) {
					pitSimServerPlayer.sendMessage(components);
				}
				if(overworldServer.getPlayers().contains(player)) {
					isOnline = true;
				}
			}

			for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
				for(ProxiedPlayer darkzoneServerPlayer : darkzoneServer.getPlayers()) {
					darkzoneServerPlayer.sendMessage(components);
				}
				if(darkzoneServer.getPlayers().contains(player)) {
					isOnline = true;
				}
			}

			PluginMessage responseMessage = new PluginMessage().writeBoolean(isOnline);
			event.getMessage().respond(responseMessage, serverInfo);
			AuctionAlerts.alert(playerName + " auction response is " + isOnline);

			if(isOnline) {
				ServerInfo playerServer = player.getServer().getInfo();
				PluginMessage outgoingMessage = new PluginMessage();
				outgoingMessage.writeString("AUCTION ITEM REQUEST").writeString(player.getUniqueId().toString());
				for(Integer integer : message.getIntegers()) {
					outgoingMessage.writeInt(integer);
				}
				message.addServer(playerServer);
				message.send();
			}
		}

		if(strings.size() >= 3 && strings.get(0).equals("AUCTION NOTIFY")) {
			String bidPlayer = strings.get(1);
			String itemName = strings.get(2);
			int bid = integers.get(0);

			strings.remove(0);
			strings.remove(0);
			strings.remove(0);

			BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&'
					, "&5&lDARK AUCTION! &e" + bidPlayer + " &7bid &f" + bid + " Souls &7on " + itemName));

			for(String string : strings) {
				UUID uuid = UUID.fromString(string);
				ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(uuid);
				if(player != null) {
					String server = player.getServer().getInfo().getName();
					if(server.contains("pitsim") || server.contains("darkzone")) {
						player.sendMessage(components);
					}
				}
			}
		}
	}

}
