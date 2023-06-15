package net.pitsim.bungee.pitsim;

import net.pitsim.bungee.ConfigManager;
import net.pitsim.bungee.ProxyRunnable;
import net.pitsim.bungee.objects.ServerType;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ServerChangeListener implements Listener {

	public static List<UUID> recentlyLeft = new ArrayList<>();

	@EventHandler
	public void onServerChange(ServerDisconnectEvent event) {
		if(!event.getTarget().getName().contains("pitsim") && !event.getTarget().getName().contains("darkzone")) return;

		recentlyLeft.add(event.getPlayer().getUniqueId());
		((ProxyRunnable) () -> recentlyLeft.remove(event.getPlayer().getUniqueId())).runAfter(3, TimeUnit.SECONDS);
	}

	@EventHandler
	public void onProxyJoin(PostLoginEvent event) {
		if(!ConfigManager.isDev()) return;

		((ProxyRunnable) () -> {
			PitSimServerManager.getManager(ServerType.OVERWORLD).queue(event.getPlayer(), 0, false);
		}).runAfter(1, TimeUnit.SECONDS);
	}
}
