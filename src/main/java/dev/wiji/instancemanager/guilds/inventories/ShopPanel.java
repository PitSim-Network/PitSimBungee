package dev.wiji.instancemanager.guilds.inventories;

import dev.wiji.instancemanager.guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.guilds.events.InventoryOpenEvent;
import dev.wiji.instancemanager.misc.PreparedGUI;
import dev.wiji.instancemanager.misc.PreparedInventoryPanel;
import net.md_5.bungee.api.ChatColor;

public class ShopPanel extends PreparedInventoryPanel {
	public MenuGUI menuGUI;

	public ShopPanel(PreparedGUI gui) {
		super(gui);
		this.menuGUI = (MenuGUI) gui;
	}

	@Override
	public String getName() {
		return ChatColor.GRAY + ((MenuGUI) gui).guild.name + " Guild Shop";
	}

	@Override
	public int getRows() {
		return 0;
	}

	@Override
	public void onClick(InventoryClickEvent event) { }

	@Override
	public void onOpen(InventoryOpenEvent event) { }

	@Override
	public void onClose(InventoryCloseEvent event) {
//		((ProxyRunnable) this::openPreviousGUI).runAfter(50, TimeUnit.MILLISECONDS);
	}
}
