package dev.wiji.instancemanager.guilds.inventories;

import dev.wiji.instancemanager.guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.guilds.events.InventoryOpenEvent;
import dev.wiji.instancemanager.misc.AItemStackBuilder;
import dev.wiji.instancemanager.misc.ALoreBuilder;
import dev.wiji.instancemanager.misc.PreparedGUI;
import dev.wiji.instancemanager.misc.PreparedInventoryPanel;
import dev.wiji.instancemanager.ProxyRunnable;

public class ConfirmationPanel extends PreparedInventoryPanel {
	public PreparedGUI menuGUI;
	public ProxyRunnable callback;
	public String name;

	public ConfirmationPanel(PreparedGUI gui, ProxyRunnable callback, ALoreBuilder yesLore, ALoreBuilder noLore, String name) {
		super(gui, true);
		this.menuGUI = gui;
		this.callback = callback;
		this.name = name;

		buildInventory();

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
		return name;
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
	public void onOpen(InventoryOpenEvent event) {}

	@Override
	public void onClose(InventoryCloseEvent event) {
//		((ProxyRunnable) this::openPreviousGUI).runAfter(50, TimeUnit.MILLISECONDS);
	}
}
