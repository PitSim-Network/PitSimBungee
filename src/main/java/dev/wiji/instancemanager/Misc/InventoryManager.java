package dev.wiji.instancemanager.Misc;

import dev.wiji.instancemanager.Guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryOpenEvent;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class InventoryManager implements Listener {

	static {
		((ProxyRunnable) () -> PreparedInventoryPanel.panels.removeIf(panel -> System.currentTimeMillis() -
				panel.creationTime > 1000 * 60 * 5)).runAfterEvery(1, 1, TimeUnit.MINUTES);

	}

	@EventHandler
	public static void onClick(InventoryClickEvent event) {

		PreparedInventoryPanel panel;
		for(PreparedInventoryPanel preparedInventoryPanel : PreparedInventoryPanel.panels) {
			if(preparedInventoryPanel.player.equals(event.getPlayer()) && preparedInventoryPanel.getName().equals(event.getInventoryName())) {
				panel = preparedInventoryPanel;
				panel.onClick(event);
				return;
			}
		}
	}

	@EventHandler
	public static void onOpen(InventoryOpenEvent event) {


	}

	@EventHandler
	public static void onClose(InventoryCloseEvent event) {
		PreparedInventoryPanel panel;
		for(PreparedInventoryPanel preparedInventoryPanel : PreparedInventoryPanel.panels) {
			if(preparedInventoryPanel.player.equals(event.getPlayer())) {
				panel = preparedInventoryPanel;
				panel.onClose(event);
				return;
			}
		}
	}
}
