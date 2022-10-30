package dev.wiji.instancemanager.Guilds.inventories;

import dev.wiji.instancemanager.Guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.Guilds.enums.DyeColor;
import dev.wiji.instancemanager.Guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryOpenEvent;
import dev.wiji.instancemanager.Misc.PreparedGUI;
import dev.wiji.instancemanager.Misc.PreparedInventoryPanel;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.ChatColor;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DyePanel extends PreparedInventoryPanel {
	public MenuGUI menuGUI;

	public DyePanel(PreparedGUI gui) {
		super(gui);
		this.menuGUI = (MenuGUI) gui;

		for(int i = 0; i < 16; i++) {
			DummyItemStack itemStack = new DummyItemStack("BANNER");
			itemStack.addModifier("BANNER_COLOR:" + DyeColor.values()[i].getDyeData());
			String name = ChatColor.translateAlternateColorCodes('&', "&f&l" + DyeColor.values()[i].name());
			itemStack.setDisplayName(name);

			getInventory().put(i, itemStack);
		}
	}

	@Override
	public String getName() {
		return ChatColor.GRAY + ((MenuGUI) gui).guild.name + " Banner Color";
	}

	@Override
	public int getRows() {
		return 2;
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		DummyItemStack clickedItem = event.getItem();
		if(clickedItem == null || !Objects.equals(clickedItem.getMaterial(), "BANNER")) return;
		menuGUI.guild.bannerColor = clickedItem.getBannerColor().getWoolData();
		menuGUI.guild.save();
		menuGUI.menuPanel.setInventory();
		openPreviousGUI();
	}

	@Override
	public void onOpen(InventoryOpenEvent event) { }

	@Override
	public void onClose(InventoryCloseEvent event) {
		((ProxyRunnable) this::openPreviousGUI).runAfter(50, TimeUnit.MILLISECONDS);
	}
}
