package dev.wiji.instancemanager.PitSim;

import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServerChangeListener implements Listener {

	public static List<ProxiedPlayer> recentlyLeft = new ArrayList<>();

	@EventHandler
	public void onServerChange(ServerDisconnectEvent event) {
		if(!event.getTarget().getName().contains("pitsim") && !event.getTarget().getName().contains("darkzone")) return;

		recentlyLeft.add(event.getPlayer());
		((ProxyRunnable) () -> recentlyLeft.remove(event.getPlayer())).runAfter(5, TimeUnit.SECONDS);
	}
}
