package dev.wiji.instancemanager.Guilds.inventories;

import dev.wiji.instancemanager.Guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryOpenEvent;
import dev.wiji.instancemanager.Misc.PreparedGUI;
import dev.wiji.instancemanager.Misc.PreparedInventoryPanel;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.ChatColor;

import java.util.concurrent.TimeUnit;

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
		((ProxyRunnable) this::openPreviousGUI).runAfter(50, TimeUnit.MILLISECONDS);
	}
}
