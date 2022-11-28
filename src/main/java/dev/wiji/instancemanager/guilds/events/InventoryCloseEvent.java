package dev.wiji.instancemanager.guilds.events;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class InventoryCloseEvent extends Event {

	private final ProxiedPlayer player;
	private final String inventoryName;

	public InventoryCloseEvent(ProxiedPlayer player, String inventoryName) {
		this.player = player;
		this.inventoryName = inventoryName;
	}

	public ProxiedPlayer getPlayer() {
		return player;
	}

	public String getInventoryName() {
		return inventoryName;
	}

}
