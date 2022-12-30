package dev.wiji.instancemanager.guilds;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.guilds.controllers.GuildManager;
import dev.wiji.instancemanager.guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.objects.DarkzoneServer;
import dev.wiji.instancemanager.objects.MainGamemodeServer;
import dev.wiji.instancemanager.objects.OverworldServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.DarkzoneServerManager;
import dev.wiji.instancemanager.pitsim.OverworldServerManager;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GuildMessaging implements Listener {

	public static Map<ProxiedPlayer, Callback> waitingForBalance = new HashMap<>();
	public static Map<ProxiedPlayer, Callback> waitingForWithdraw = new HashMap<>();

	static {
		((ProxyRunnable) () -> {
			for(OverworldServer overworldServer : OverworldServerManager.serverList) {
				if(!overworldServer.status.isOnline()) continue;
				sendGuildLeaderboardData();
			}

			for(MainGamemodeServer server : MainGamemodeServer.serverList) {
				for(ProxiedPlayer player : server.getPlayers()) {
					GuildMessaging.sendGuildData(player, server);
				}
			}
		}).runAfterEvery(15, 15, TimeUnit.SECONDS);
	}

	@EventHandler
	public void onMessageReceived(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();
		List<Integer> integers = message.getIntegers();
		List<Boolean> booleans = message.getBooleans();

		if(strings.size() >= 1 && strings.get(0).equals("INVENTORY CLICK")) {

			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(UUID.fromString(strings.get(1)));
			if(player == null) return;
			String inventoryName = strings.get(2);
			int slot = integers.get(0);
			DummyItemStack itemStack = DummyItemStack.fromString(strings.get(3));

			InventoryClickEvent inventoryClickEvent = new InventoryClickEvent(player, slot, itemStack, inventoryName);
			BungeeMain.INSTANCE.getProxy().getPluginManager().callEvent(inventoryClickEvent);
		}

		if(strings.size() >= 1 && strings.get(0).equals("INVENTORY CLOSE")) {

			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(UUID.fromString(strings.get(1)));
			if(player == null) return;
			String inventoryName = strings.get(2);

			InventoryCloseEvent inventoryClickEvent = new InventoryCloseEvent(player, inventoryName);
			BungeeMain.INSTANCE.getProxy().getPluginManager().callEvent(inventoryClickEvent);
		}

		if(strings.size() >= 2 && strings.get(0).equals("ADD REPUTATION")) {
			Guild guild = GuildManager.getGuildFromGuildUUID(UUID.fromString(strings.get(1)));
			if(guild == null) return;
			guild.addReputation(integers.get(0));
		}

		if(strings.size() >= 2 && strings.get(0).equals("DEPOSIT")) {
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(UUID.fromString(strings.get(1)));
			if(player == null) return;

			boolean success = booleans.get(0);
			if(waitingForBalance.containsKey(player)) {
				if(success) waitingForBalance.get(player).success.run();
				else waitingForBalance.get(player).fail.run();
				waitingForBalance.remove(player);
			}
		}

		if(strings.size() >= 2 && strings.get(0).equals("WITHDRAW")) {
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(UUID.fromString(strings.get(1)));
			if(player == null) return;

			boolean success = booleans.get(0);
			if(waitingForWithdraw.containsKey(player)) {
				if(success) waitingForWithdraw.get(player).success.run();
				else waitingForWithdraw.get(player).fail.run();
				waitingForWithdraw.remove(player);
			}
		}
	}

	public static void sendGuildLeaderboardData() {
		PluginMessage message = new PluginMessage().writeString("GUILD LEADERBOARD DATA");

		for(int i = 0; i < 10; i++) {
			if(i >= GuildManager.guildList.size()) break;
			Guild guild = GuildManager.getTopGuilds().get(i);
			message.writeString(guild.uuid.toString());
			message.writeString(guild.name);
			message.writeInt(guild.reputation);
			message.writeString(guild.getColor().name());
		}

		for(OverworldServer overworldServer : OverworldServerManager.serverList) {
			if(!overworldServer.status.isOnline()) continue;
			message.addServer(overworldServer.getServerInfo());
		}

		message.send();
	}

	public static void withdraw(ProxiedPlayer player, int amount, ProxyRunnable success, ProxyRunnable fail, boolean lol) {

		if(waitingForWithdraw.containsKey(player)) {
			fail.run();
			return;
		}


		waitingForWithdraw.put(player, new Callback(success, fail));

		((ProxyRunnable) () -> {
			if(!waitingForWithdraw.containsKey(player)) return;
			waitingForWithdraw.remove(player);
			fail.run();
		}).runAfter(1, TimeUnit.SECONDS);

		PluginMessage message = new PluginMessage().writeString("WITHDRAW").writeString(player.getUniqueId().toString());
		message.writeInt(amount);
		message.addServer(player.getServer().getInfo());
		message.send();

	}

	public static void sendGuildData(ProxiedPlayer player) {

		Guild guild = GuildManager.getGuildFromPlayer(player.getUniqueId());
		if(guild == null) return;

		PluginMessage message = new PluginMessage().writeString("GUILD DATA");
		message.writeString(player.getUniqueId().toString());

		message.writeString(guild.uuid.toString());

		message.writeString(guild.tag);

		message.writeString(guild.getColor().name());

		for(Integer value : guild.buffLevels.values()) {
			message.writeInt(value);
		}

		for(OverworldServer overworldServer : OverworldServerManager.serverList) {
			if(overworldServer.status.isOnline()) message.addServer(overworldServer.getServerInfo());
		}

		for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
			if(darkzoneServer.status.isOnline()) message.addServer(darkzoneServer.getServerInfo());
		}
		message.send();
	}

	public static void sendGuildData(ProxiedPlayer player, MainGamemodeServer server) {

		Guild guild = GuildManager.getGuildFromPlayer(player.getUniqueId());
		if(guild == null) return;

		PluginMessage message = new PluginMessage().writeString("GUILD DATA");
		message.writeString(player.getUniqueId().toString());

		message.writeString(guild.uuid.toString());

		message.writeString(guild.tag);

		message.writeString(guild.getColor().name());

		for(Integer value : guild.buffLevels.values()) {
			message.writeInt(value);
		}

		if(server.status.isOnline()) {
			message.addServer(server.getServerInfo());
			message.send();
		}
	}

	public static void deposit(ProxiedPlayer player, int amount, ProxyRunnable success, ProxyRunnable fail) {
		if(waitingForBalance.containsKey(player)) {
			fail.run();
			return;
		}

		waitingForBalance.put(player, new Callback(success, fail));

		((ProxyRunnable) () -> {
			if(!waitingForBalance.containsKey(player)) return;
			waitingForBalance.remove(player);
			fail.run();
		}).runAfter(1, TimeUnit.SECONDS);

		PluginMessage message = new PluginMessage().writeString("DEPOSIT").writeString(player.getUniqueId().toString());
		message.writeInt(amount);
		message.addServer(player.getServer().getInfo());
		message.send();
	}

	public static class Callback {

		public ProxyRunnable success;
		public ProxyRunnable fail;

		public Callback(ProxyRunnable success, ProxyRunnable fail) {
			this.success = success;
			this.fail = fail;
		}
	}
}
