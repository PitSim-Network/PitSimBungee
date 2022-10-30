package dev.wiji.instancemanager.Guilds.inventories;

import dev.wiji.instancemanager.Guilds.controllers.UpgradeManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildUpgrade;
import dev.wiji.instancemanager.Guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryOpenEvent;
import dev.wiji.instancemanager.Misc.AOutput;
import dev.wiji.instancemanager.Misc.AUtil;
import dev.wiji.instancemanager.Misc.PreparedGUI;
import dev.wiji.instancemanager.Misc.PreparedInventoryPanel;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.ChatColor;

import java.util.concurrent.TimeUnit;

public class UpgradePanel extends PreparedInventoryPanel {
	public MenuGUI menuGUI;

	public UpgradePanel(PreparedGUI gui) {
		super(gui);
		this.menuGUI = (MenuGUI) gui;

		inventoryBuilder.createBorder("STAINED_GLASS_PANE", 7, getRows() * 9);

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
			updateInventory();

			playSound("UPGRADE");
			menuGUI.guild.broadcast("&a&lGUILD! &7Upgraded " + upgrade.displayName + " &7to level &a" + AUtil.toRoman(level + 1));
		}
	}

	@Override
	public void onOpen(InventoryOpenEvent event) { }

	@Override
	public void onClose(InventoryCloseEvent event) {
		((ProxyRunnable) this::openPreviousGUI).runAfter(50, TimeUnit.MILLISECONDS);
	}
}
