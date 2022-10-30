package dev.wiji.instancemanager.Misc;

import dev.wiji.instancemanager.Guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryOpenEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

@SuppressWarnings("unused")
public class InventoryManager implements Listener {

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
