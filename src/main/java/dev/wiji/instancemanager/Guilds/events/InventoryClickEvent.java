package dev.wiji.instancemanager.Guilds.events;

import dev.wiji.instancemanager.Guilds.controllers.objects.DummyItemStack;
import net.md_5.bungee.api.plugin.Event;

public class InventoryClickEvent extends Event {

	private final int slot;
	private final DummyItemStack item;
	public InventoryClickEvent(int slot, DummyItemStack clickedItem) {
		this.slot = slot;
		this.item = clickedItem;
	}

	public int getSlot() {
		return slot;
	}

	public DummyItemStack getCurrentItem() {
		return item;
	}

}
