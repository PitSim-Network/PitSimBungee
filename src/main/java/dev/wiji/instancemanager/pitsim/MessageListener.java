package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.objects.DarkzoneServer;
import dev.wiji.instancemanager.objects.PitSimServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.objects.ServerStatus;
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
			for(PitSimServer server : PitSimServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					if(PitSimServerManager.networkIsShuttingDown || server.status.isShuttingDown()) {
						server.hardShutDown();
					} else {
						System.out.println("Server " + serverName + " is now running!");

						BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&2&lKEEPER! &7Server &e" + serverName + " &7is now available!"));
						for(ProxiedPlayer player : BungeeMain.INSTANCE.getProxy().getPlayers()) {
							if(player.getServer().getInfo().getName().contains("pitsim")) {
								player.sendMessage(components);
							}
						}

						server.status = ServerStatus.RUNNING;
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
						break;
					}
				}
			}

		}

		if(strings.size() >= 2 && strings.get(0).equals("INITIATE FINAL SHUTDOWN")) {
			String serverName = strings.get(1);
				for(PitSimServer server : PitSimServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					server.serverData = null;
					server.status = ServerStatus.SHUTTING_DOWN_FINAL;
					server.beginStartCooldown();
				}
			}

			for(DarkzoneServer server : DarkzoneServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					server.serverData = null;
					server.status = ServerStatus.SHUTTING_DOWN_FINAL;
					server.beginStartCooldown();
				}
			}

		}

		if(strings.size() >= 2 && strings.get(0).equals("INITIATE FINAL RESTART")) {
			String serverName = strings.get(1);
			for(PitSimServer server : PitSimServerManager.serverList) {
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
			for(PitSimServer server : PitSimServerManager.serverList) {
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

			PitSimServerManager.queue(player, requested, fromDarkzone);
		}

		if(strings.size() >= 2 && strings.get(0).equals("QUEUE DARKZONE")) {

			String playerString = strings.get(1);
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerString);
			if(player == null) return;

			int requested = 0;
			if(integers.size() >= 1) {
				requested = integers.get(0);
			}

			DarkzoneServerManager.queue(player, requested);
		}

		if(strings.size() >= 3 && strings.get(0).equals("BOOSTER USE")) {
			String boosterName = strings.get(1);
			String announcement = strings.get(2);

			PluginMessage outgoingMessage = new PluginMessage();
			outgoingMessage.writeString("BOOSTER USE").writeString(boosterName).writeString(announcement);
			for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
				if(pitSimServer.status.isOnline()) message.addServer(pitSimServer.getServerInfo());
			}

			message.send();
		}

		if(strings.size() >= 3 && strings.get(0).equals("AUCTION ITEM REQUEST")) {
			String playerName = strings.get(1);
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerName);
			ServerInfo serverInfo = BungeeMain.INSTANCE.getProxy().getServerInfo(strings.get(2));

			BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&'
					,"&5&lDARK AUCTION! &e" + playerName + " &7won " + strings.get(3) + " &7for &f" +
							integers.get(2) + " Souls&7."));

			boolean isOnline = false;

			for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
				for(ProxiedPlayer pitSimServerPlayer : pitSimServer.getPlayers()) {
					pitSimServerPlayer.sendMessage(components);
				}
				if(pitSimServer.getPlayers().contains(player)) {
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
					,"&5&lDARK AUCTION! &e" + bidPlayer + " &7bid &f" + bid + " Souls &7on " + itemName));

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
