package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.auctions.AuctionManager;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.market.MarketManager;
import dev.wiji.instancemanager.objects.PitSimServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.objects.ServerStatus;
import dev.wiji.instancemanager.objects.ServerType;
import dev.wiji.instancemanager.storage.EditSessionManager;
import dev.wiji.instancemanager.storage.StorageManager;
import dev.wiji.instancemanager.storage.StorageProfile;
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
			for(PitSimServer server : PitSimServerManager.mixedServerList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					if(PitSimServerManager.networkIsShuttingDown || server.status.isShuttingDown()) {
						server.hardShutDown();
					} else {
						System.out.println("Server " + serverName + " is now running!");

						for(ProxiedPlayer player : server.getPlayers()) {
							StorageProfile profile = StorageManager.getStorage(player.getUniqueId());
							profile.sendToServer(server.getServerInfo());
						}

						AuctionManager.sendAuctionsToServer(serverName);

						BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&2&lKEEPER! &7Server &e" + serverName + " &7is now available!"));
						for(ProxiedPlayer player : BungeeMain.INSTANCE.getProxy().getPlayers()) {
							if(player.getServer().getInfo().getName().contains("pitsim")) {
								player.sendMessage(components);
							}
						}

						if(server.status != ServerStatus.SUSPENDED) server.status = ServerStatus.RUNNING;
						server.setStartTime(System.currentTimeMillis());
						if(server.serverType == ServerType.DARKZONE) MarketManager.updateAll();
						break;
					}
				}
			}
		}

		if(strings.size() >= 2 && strings.get(0).equals("INITIATE FINAL SHUTDOWN")) {
			String serverName = strings.get(1);
			for(PitSimServer server : PitSimServerManager.mixedServerList) {
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
			for(PitSimServer server : PitSimServerManager.mixedServerList) {
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
			for(PitSimServer server : PitSimServerManager.mixedServerList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					server.status = ServerStatus.valueOf(status);
					break;
				}
			}
		}


		if(strings.size() >= 2 && strings.get(0).startsWith("QUEUE")) {
			ServerType serverType = strings.get(0).contains("DARKZONE") ? ServerType.DARKZONE : ServerType.OVERWORLD;

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

			PitSimServerManager manager = PitSimServerManager.getManager(serverType);
			assert manager != null;
			manager.queueFallback(player, requested, fromDarkzone);
		}

		if(strings.size() >= 3 && strings.get(0).equals("BOOSTER USE")) {
			String boosterName = strings.get(1);
			String announcement = strings.get(2);
			String activatorUUID = strings.get(3);
			int time = integers.get(0);

			PluginMessage outgoingMessage = new PluginMessage();
			outgoingMessage.writeString("BOOSTER USE")
					.writeString(boosterName)
					.writeString(announcement)
					.writeString(activatorUUID)
					.writeInt(time);
			for(PitSimServer pitSimServer : PitSimServerManager.mixedServerList) {
				if(pitSimServer.status.isOnline()) message.addServer(pitSimServer.getServerInfo());
			}

			message.send();
		}

		if(strings.size() >= 1 && strings.get(0).equals("BOOSTER_SHARE")) {
			String boosterName = strings.get(1);
			UUID activatorUUID = UUID.fromString(strings.get(2));
			int amount = integers.get(0);

			ProxiedPlayer proxiedPlayer = BungeeMain.INSTANCE.getProxy().getPlayer(activatorUUID);
			if(proxiedPlayer == null) return;

			PluginMessage outgoingMessage = new PluginMessage();
			outgoingMessage.writeString("BOOSTER_SHARE")
					.writeString(boosterName)
					.writeString(activatorUUID.toString())
					.writeInt(amount);
			for(PitSimServer pitSimServer : PitSimServerManager.mixedServerList) {
				if(!pitSimServer.status.isOnline() || !pitSimServer.getPlayers().contains(proxiedPlayer)) continue;
				message.addServer(pitSimServer.getServerInfo());
				break;
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

			ServerInfo serverInfo = PitSimServer.getServer(serverIndex + 1, darkzone).getServerInfo();
			response.addServer(serverInfo).send();
		}
	}

}
