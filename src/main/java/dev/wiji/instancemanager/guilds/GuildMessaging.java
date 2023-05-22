package dev.wiji.instancemanager.guilds;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.guilds.controllers.GuildManager;
import dev.wiji.instancemanager.guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.objects.*;
import dev.wiji.instancemanager.pitsim.MessageListener;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GuildMessaging implements Listener {

	public static PitSimServerManager overworldManager = PitSimServerManager.getManager(ServerType.OVERWORLD);

	public static Map<ProxiedPlayer, Callback> waitingForBalance = new HashMap<>();
	public static Map<ProxiedPlayer, Callback> waitingForWithdraw = new HashMap<>();

	static {
		((ProxyRunnable) () -> {

			for(PitSimServer overworldServer : overworldManager.serverList) {
				if(!overworldServer.status.isOnline()) continue;
				sendGuildLeaderboardData();
			}

			for(PitSimServer server : PitSimServerManager.mixedServerList) {
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
			executeCallback(strings, booleans, waitingForBalance);
		}

		if(strings.size() >= 2 && strings.get(0).equals("WITHDRAW")) {
			executeCallback(strings, booleans, waitingForWithdraw);
		}

		if(strings.size() >= 1 && strings.get(0).equals("OUTPOST DATA")) {
			if(strings.get(1) == null) return;


			String guildUUID = strings.get(1);
			boolean isActive = booleans.get(0);

			if(isActive && !guildUUID.equals("null")) ConfigManager.setControllingGuild(UUID.fromString(guildUUID));

			MessageListener.sendOutpostData(guildUUID, isActive, false);
		}

		if(strings.size() >= 1 && strings.get(0).equals("GUILD MESSAGE")) {
			UUID guildUUID = UUID.fromString(strings.get(1));
			Guild guild = GuildManager.getGuildFromGuildUUID(guildUUID);
			String guildMessage = strings.get(2);

			if(guild == null) return;
			guild.broadcast("&8[&6Guild&8] " + guildMessage);
		}

		if(strings.size() >= 1 && strings.get(0).equals("OUTPOST GOLD")) {
			UUID guildUUID = UUID.fromString(strings.get(1));
			Guild guild = GuildManager.getGuildFromGuildUUID(guildUUID);
			String notification = strings.get(2);
			int amount = integers.get(0);

			if(guild == null) return;

			if(amount > guild.getMaxBank() - guild.getBalance()) {
				amount = (int) (guild.getMaxBank() - guild.getBalance());
			}

			guild.deposit(amount);

			guild.broadcast("&8[&6Guild&8] " + notification);
		}
	}

	private void executeCallback(List<String> strings, List<Boolean> booleans, Map<ProxiedPlayer, Callback> waitingForWithdraw) {
		ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(UUID.fromString(strings.get(1)));
		if(player == null) return;

		boolean success = booleans.get(0);
		if(waitingForWithdraw.containsKey(player)) {
			if(success) waitingForWithdraw.get(player).success.run();
			else waitingForWithdraw.get(player).fail.run();
			waitingForWithdraw.remove(player);
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

		for(PitSimServer overworldServer : overworldManager.serverList) {
			if(!overworldServer.status.isOnline()) continue;
			message.addServer(overworldServer.getServerInfo());
		}

		message.send();
	}

	public static void withdraw(ProxiedPlayer player, int amount, ProxyRunnable success, ProxyRunnable fail) {
		if(executeCallbacks(player, success, fail, waitingForWithdraw)) return;

		PluginMessage message = new PluginMessage().writeString("WITHDRAW").writeString(player.getUniqueId().toString());
		message.writeInt(amount);
		message.addServer(player.getServer().getInfo());
		message.send();
	}

	public static void sendGuildData(ProxiedPlayer player) {
		sendGuildData(player, null);
	}

	public static void sendGuildData(ProxiedPlayer player, PitSimServer server) {

		Guild guild = GuildManager.getGuildFromPlayer(player.getUniqueId());
		if(guild == null) return;

		PluginMessage message = new PluginMessage().writeString("GUILD DATA");
		message.writeString(player.getUniqueId().toString());

		message.writeString(guild.uuid.toString());

		message.writeString(guild.tag);

		message.writeString(guild.getColor().name());

		message.writeString(guild.name);

		for(Integer value : guild.buffLevels.values()) {
			message.writeInt(value);
		}

		if(server == null) {
			for(PitSimServer pitSimServer : PitSimServerManager.mixedServerList) {
				if(pitSimServer.status.isOnline()) message.addServer(pitSimServer.getServerInfo());
			}
		} else if(server.status.isOnline()) {
			message.addServer(server.getServerInfo());
		}

		message.send();
	}

	public static void deposit(ProxiedPlayer player, int amount, ProxyRunnable success, ProxyRunnable fail) {
		if(executeCallbacks(player, success, fail, waitingForBalance)) return;

		PluginMessage message = new PluginMessage().writeString("DEPOSIT").writeString(player.getUniqueId().toString());
		message.writeInt(amount);
		message.addServer(player.getServer().getInfo());
		message.send();
	}

	private static boolean executeCallbacks(ProxiedPlayer player, ProxyRunnable success, ProxyRunnable fail, Map<ProxiedPlayer, Callback> waitingForBalance) {
		if(waitingForBalance.containsKey(player)) {
			fail.run();
			return true;
		}

		waitingForBalance.put(player, new Callback(success, fail));

		((ProxyRunnable) () -> {
			if(!waitingForBalance.containsKey(player)) return;
			waitingForBalance.remove(player);
			fail.run();
		}).runAfter(1, TimeUnit.SECONDS);
		return false;
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
