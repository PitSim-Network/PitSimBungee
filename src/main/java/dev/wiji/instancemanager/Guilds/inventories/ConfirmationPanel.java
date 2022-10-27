package dev.wiji.instancemanager.Guilds.inventories;

import dev.wiji.instancemanager.Guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.Guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryOpenEvent;
import dev.wiji.instancemanager.Misc.AItemStackBuilder;
import dev.wiji.instancemanager.Misc.ALoreBuilder;
import dev.wiji.instancemanager.Misc.PreparedGUI;
import dev.wiji.instancemanager.Misc.PreparedInventoryPanel;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.ChatColor;

import java.util.concurrent.TimeUnit;

public class ConfirmationPanel extends PreparedInventoryPanel {
	public PreparedGUI menuGUI;
	public ProxyRunnable callback;

	public ConfirmationPanel(PreparedGUI gui, ProxyRunnable callback, ALoreBuilder yesLore, ALoreBuilder noLore) {
		super(gui);
		this.menuGUI = gui;
		this.callback = callback;

		DummyItemStack yes = new AItemStackBuilder("STAINED_CLAY", 1, 5)
				.setName("&a&lCONFIRM")
				.setLore(yesLore)
				.getItemStack();
		getInventory().put(11, yes);

		DummyItemStack no = new AItemStackBuilder("STAINED_CLAY", 1, 14)
				.setName("&c&lDECLINE")
				.setLore(noLore)
				.getItemStack();
		getInventory().put(15, no);
	}

	@Override
	public String getName() {
		return ChatColor.GRAY + "Confirmation GUI";
	}

	@Override
	public int getRows() {
		return 3;
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		int slot = event.getSlot();
		if(slot == 11) {
			callback.run();
			closeInventory();
		} else if(slot == 15) {
			closeInventory();
		}
	}

	@Override
	public void onOpen(InventoryOpenEvent event) { }

	@Override
	public void onClose(InventoryCloseEvent event) {
		((ProxyRunnable) this::openPreviousGUI).runAfter(50, TimeUnit.MILLISECONDS);
	}
}
