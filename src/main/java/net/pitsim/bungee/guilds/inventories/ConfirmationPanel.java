package net.pitsim.bungee.guilds.inventories;

import net.pitsim.bungee.guilds.controllers.objects.DummyItemStack;
import net.pitsim.bungee.guilds.events.InventoryClickEvent;
import net.pitsim.bungee.guilds.events.InventoryCloseEvent;
import net.pitsim.bungee.guilds.events.InventoryOpenEvent;
import net.pitsim.bungee.misc.AItemStackBuilder;
import net.pitsim.bungee.misc.ALoreBuilder;
import net.pitsim.bungee.misc.PreparedGUI;
import net.pitsim.bungee.misc.PreparedInventoryPanel;
import net.pitsim.bungee.ProxyRunnable;

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
