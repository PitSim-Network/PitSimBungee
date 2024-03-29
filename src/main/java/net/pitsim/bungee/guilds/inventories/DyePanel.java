package net.pitsim.bungee.guilds.inventories;

import net.pitsim.bungee.guilds.controllers.objects.DummyItemStack;
import net.pitsim.bungee.guilds.enums.DyeColor;
import net.pitsim.bungee.guilds.events.InventoryClickEvent;
import net.pitsim.bungee.guilds.events.InventoryCloseEvent;
import net.pitsim.bungee.guilds.events.InventoryOpenEvent;
import net.pitsim.bungee.misc.PreparedGUI;
import net.pitsim.bungee.misc.PreparedInventoryPanel;
import net.md_5.bungee.api.ChatColor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

		DummyItemStack back = new DummyItemStack("ARROW");
		back.setDisplayName(ChatColor.GREEN + "Go Back!");
		List<String> lore = Collections.singletonList(ChatColor.GRAY + "To Guild Menu");
		back.setLore(lore);

		getInventory().put(22, back);
	}

	@Override
	public String getName() {
		return ChatColor.GRAY + ((MenuGUI) gui).guild.name + " Banner Color";
	}

	@Override
	public int getRows() {
		return 3;
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		DummyItemStack clickedItem = event.getItem();

		if(event.getSlot() == 22) {
			openPreviousGUI();
			return;
		}

		if(clickedItem == null || !Objects.equals(clickedItem.getMaterial(), "BANNER")) return;
		menuGUI.guild.bannerColor = clickedItem.getBannerColor().getWoolData();
		menuGUI.guild.save();
		menuGUI.menuPanel.setInventory();
		openPreviousGUI();
	}

	@Override
	public void onOpen(InventoryOpenEvent event) {}

	@Override
	public void onClose(InventoryCloseEvent event) {
//		((ProxyRunnable) this::openPreviousGUI).runAfter(50, TimeUnit.MILLISECONDS);
	}
}
