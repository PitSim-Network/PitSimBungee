package dev.wiji.instancemanager.Guilds.events;

import dev.wiji.instancemanager.Guilds.controllers.objects.DummyItemStack;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class InventoryClickEvent extends Event {

	private final ProxiedPlayer player;
	private final int slot;
	private final DummyItemStack item;
	private final String inventoryName;

	public InventoryClickEvent(ProxiedPlayer player, int slot, DummyItemStack clickedItem, String inventoryName) {
		this.player = player;
		this.slot = slot;
		this.item = clickedItem;
		this.inventoryName = inventoryName;
	}

	public int getSlot() {
		return slot;
	}

	public DummyItemStack getItem() {
		return item;
	}

	public ProxiedPlayer getPlayer() {
		return player;
	}

	public String getInventoryName() {
		return inventoryName;
	}

}
