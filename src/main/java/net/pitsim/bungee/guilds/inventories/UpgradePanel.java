package net.pitsim.bungee.guilds.inventories;

import net.pitsim.bungee.guilds.controllers.UpgradeManager;
import net.pitsim.bungee.guilds.controllers.objects.DummyItemStack;
import net.pitsim.bungee.guilds.controllers.objects.GuildUpgrade;
import net.pitsim.bungee.guilds.events.InventoryClickEvent;
import net.pitsim.bungee.guilds.events.InventoryCloseEvent;
import net.pitsim.bungee.guilds.events.InventoryOpenEvent;
import net.pitsim.bungee.misc.AOutput;
import net.pitsim.bungee.misc.AUtil;
import net.pitsim.bungee.misc.PreparedGUI;
import net.pitsim.bungee.misc.PreparedInventoryPanel;
import net.md_5.bungee.api.ChatColor;

import java.util.Collections;
import java.util.List;

public class UpgradePanel extends PreparedInventoryPanel {
	public MenuGUI menuGUI;

	public UpgradePanel(PreparedGUI gui) {
		super(gui);
		this.menuGUI = (MenuGUI) gui;

		inventoryBuilder.createBorder("STAINED_GLASS_PANE", 7, getRows() * 9);

		DummyItemStack back = new DummyItemStack("ARROW");
		back.setDisplayName(ChatColor.GREEN + "Go Back!");
		List<String> lore = Collections.singletonList(ChatColor.GRAY + "To Guild Menu");
		back.setLore(lore);

		getInventory().put(22, back);

		for(int i = 0; i < UpgradeManager.upgradeList.size(); i++) {
			GuildUpgrade upgrade = UpgradeManager.upgradeList.get(i);
			int level = menuGUI.guild.getLevel(upgrade);
			getInventory().put(upgrade.slot, upgrade.getDisplayStack(menuGUI.guild, level));
		}
	}

	@Override
	public String getName() {
		return ChatColor.GRAY + ((MenuGUI) gui).guild.name + " Guild Settings";
	}

	@Override
	public int getRows() {
		return 3;
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		int slot = event.getSlot();

		if(event.getSlot() == 22) {
			openPreviousGUI();
			return;
		}

		for(GuildUpgrade upgrade : UpgradeManager.upgradeList) {
			if(upgrade.slot != slot) continue;

			int level = menuGUI.guild.getLevel(upgrade);
			if(level >= upgrade.maxLevel) {
				AOutput.error(player, "That upgrade is max level");
				return;
			}

			long balance = menuGUI.guild.getBalance();
			int cost = upgrade.getCost(level + 1);
			if(balance < cost) {
				AOutput.error(player, "Insufficient bank funds");
				return;
			}

			menuGUI.guild.withdraw(cost);
			menuGUI.guild.upgradeLevels.put(upgrade, level + 1);
			menuGUI.guild.save();

			getInventory().put(upgrade.slot, upgrade.getDisplayStack(menuGUI.guild, level + 1));
			updateInventory(new UpgradePanel(gui));

			playSound("UPGRADE");
			menuGUI.guild.broadcast("&a&lGUILD! &7Upgraded " + upgrade.displayName + " &7to level &a" + AUtil.toRoman(level + 1));
		}
	}

	@Override
	public void onOpen(InventoryOpenEvent event) {}

	@Override
	public void onClose(InventoryCloseEvent event) {
//		((ProxyRunnable) this::openPreviousGUI).runAfter(50, TimeUnit.MILLISECONDS);
	}
}
