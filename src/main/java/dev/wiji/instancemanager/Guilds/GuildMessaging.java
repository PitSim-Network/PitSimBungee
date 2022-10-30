package dev.wiji.instancemanager.Guilds;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Events.MessageEvent;
import dev.wiji.instancemanager.Guilds.controllers.GuildManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import dev.wiji.instancemanager.Guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.Objects.DarkzoneServer;
import dev.wiji.instancemanager.Objects.PitSimServer;
import dev.wiji.instancemanager.Objects.PluginMessage;
import dev.wiji.instancemanager.PitSim.DarkzoneServerManager;
import dev.wiji.instancemanager.PitSim.PitSimServerManager;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GuildMessaging implements Listener {

	static {
		((ProxyRunnable) () -> {
			for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
				if(!pitSimServer.status.isOnline()) continue;
				sendGuildLeaderboardData();
			}
		}).runAfterEvery(15, 15, TimeUnit.SECONDS);
	}

	@EventHandler
	public void onMessageReceived(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();
		List<Integer> integers = message.getIntegers();

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

		for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
			if(!pitSimServer.status.isOnline()) continue;
			message.addServer(pitSimServer.getServerInfo());
		}

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

		for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
			if(pitSimServer.status.isOnline()) message.addServer(pitSimServer.getServerInfo());
		}

		for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
			if(darkzoneServer.status.isOnline()) message.addServer(darkzoneServer.getServerInfo());
		}
		message.send();
	}
}
