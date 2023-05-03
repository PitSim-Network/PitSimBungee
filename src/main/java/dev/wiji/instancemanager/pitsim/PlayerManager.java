package dev.wiji.instancemanager.pitsim;

import de.myzelyam.api.vanish.BungeePlayerHideEvent;
import de.myzelyam.api.vanish.BungeePlayerShowEvent;
import de.myzelyam.api.vanish.BungeeVanishAPI;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.PitSimServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

@SuppressWarnings("Duplicates")
public class PlayerManager implements Listener {

	@EventHandler
	public void onServerJoin(ServerConnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		ServerInfo previousServer = event.getPlayer().getServer() == null ? null : event.getPlayer().getServer().getInfo();
		ServerInfo targetServer = event.getTarget();

		if(BungeeVanishAPI.isInvisible(player)) return;

		for(PitSimServer server : PitSimServerManager.mixedServerList) if(server.getServerInfo().equals(previousServer)) return;
		for(PitSimServer server : PitSimServerManager.mixedServerList) {
			if(!server.getServerInfo().equals(targetServer)) continue;
			sendJoinMessage(player);
			return;
		}
	}

	@EventHandler
	public void onLeave(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		if(event.getPlayer() == null || event.getPlayer().getServer() == null) return;
		ServerInfo previousServer = event.getPlayer().getServer().getInfo();

		if(BungeeVanishAPI.isInvisible(player)) return;

		for(PitSimServer server : PitSimServerManager.mixedServerList) {
			if(!server.getServerInfo().equals(previousServer)) continue;
			sendLeaveMessage(player);
			return;
		}
	}

	@EventHandler
	public void onServerLeave(ServerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		ServerInfo previousServer = event.getTarget();
		ServerInfo targetServer = event.getPlayer().getServer() == null ? null : event.getPlayer().getServer().getInfo();

		if(BungeeVanishAPI.isInvisible(player)) return;

		if(previousServer == targetServer) return;
		for(PitSimServer server : PitSimServerManager.mixedServerList) if(server.getServerInfo().equals(targetServer)) return;
		for(PitSimServer server : PitSimServerManager.mixedServerList) {
			if(!server.getServerInfo().equals(previousServer)) continue;
			sendLeaveMessage(player);
			return;
		}
	}

	@EventHandler
	public void onVanish(BungeePlayerShowEvent event) {
		ProxiedPlayer player = event.getPlayer();
		ServerInfo currentServer = event.getPlayer().getServer().getInfo();
		for(PitSimServer server : PitSimServerManager.mixedServerList) {
			if(!server.getServerInfo().equals(currentServer)) continue;
			sendJoinMessage(player);
			return;
		}
	}

	@EventHandler
	public void onVanish(BungeePlayerHideEvent event) {
		ProxiedPlayer player = event.getPlayer();
		ServerInfo currentServer = event.getPlayer().getServer().getInfo();
		for(PitSimServer server : PitSimServerManager.mixedServerList) {
			if(!server.getServerInfo().equals(currentServer)) continue;
			sendLeaveMessage(player);
			return;
		}
	}

	public void sendJoinMessage(ProxiedPlayer player) {
		for(PitSimServer server : PitSimServerManager.mixedServerList) {
			for(ProxiedPlayer serverPlayer : server.getPlayers()) {
				if(serverPlayer == player) continue;
				AOutput.color(serverPlayer, "&8[&a+&8] &6" + player.getName() + " &ehas joined");
			}
		}
	}

	public void sendLeaveMessage(ProxiedPlayer player) {
		for(PitSimServer server : PitSimServerManager.mixedServerList) {
			for(ProxiedPlayer serverPlayer : server.getPlayers()) {
				if(serverPlayer == player) continue;
				AOutput.color(serverPlayer, "&8[&c-&8] &6" + player.getName() + " &ehas left");
			}
		}
	}
}
