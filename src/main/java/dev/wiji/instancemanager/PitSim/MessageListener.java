package dev.wiji.instancemanager.PitSim;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Events.MessageEvent;
import dev.wiji.instancemanager.Objects.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

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

			boolean fromDarkzone = false;
			if(booleans.size() >= 1) {
				fromDarkzone = booleans.get(0);
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
	}

}
