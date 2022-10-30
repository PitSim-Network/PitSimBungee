package dev.wiji.instancemanager.Guilds.inventories;

import dev.wiji.instancemanager.Guilds.ArcticGuilds;
import dev.wiji.instancemanager.Guilds.controllers.BuffManager;
import dev.wiji.instancemanager.Guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.Guilds.controllers.objects.GuildBuff;
import dev.wiji.instancemanager.Guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.Guilds.events.InventoryOpenEvent;
import dev.wiji.instancemanager.Misc.*;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BuffPanel extends PreparedInventoryPanel {
	public MenuGUI menuGUI;

	public BuffPanel(PreparedGUI gui) {
		super(gui);
		this.menuGUI = (MenuGUI) gui;

		setInventory();
	}

	@Override
	public String getName() {
		return ChatColor.GRAY + ((MenuGUI) gui).guild.name + " Guild Buffs";
	}

	@Override
	public int getRows() {
		return 6;
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		int slot = event.getSlot();
		int row = slot / 9;
		int level = slot % 9;
		boolean isMain = slot % 9 == 0;
		GuildBuff buff = BuffManager.getBuff(row);
		if(buff == null) return;
		if(isMain) {
			menuGUI.guild.buffLevels.put(buff, 0);
			menuGUI.guild.save();
			menuGUI.guild.broadcast("&a&lGUILD &7Reset " + buff.displayName + " &7levels");
			setInventory();
			playSound("RESET");
		} else {
			int buffLevel = menuGUI.guild.getLevel(buff);
			int cost = buffLevel + buff.firstLevelCost;
			if(level - 1 == buffLevel) {
				if(menuGUI.guild.getTotalBuffCost() + cost > menuGUI.guild.getRepPoints()) {
					AOutput.error(player, "You do not have enough reputation points");
					return;
				}
				menuGUI.guild.buffLevels.put(buff, buffLevel + 1);
				menuGUI.guild.save();
				menuGUI.guild.broadcast("&a&lGUILD! &7Increased " + buff.displayName + " &7to level &a" + AUtil.toRoman(buffLevel + 1));
				setInventory();
				playSound("UPGRADE");
			}
		}
	}

	@Override
	public void onOpen(InventoryOpenEvent event) { }

	@Override
	public void onClose(InventoryCloseEvent event) {
		new ProxyRunnable() {
			@Override
			public void run() {
				openPreviousGUI();
			}
		}.runAfter(50, TimeUnit.MILLISECONDS);
	}

	public void setInventory() {
		for(int i = 0; i < BuffManager.buffList.size(); i++) {
			GuildBuff buff = BuffManager.buffList.get(i);
			int startingSlot = i * 9;
			int buffLevel = menuGUI.guild.getLevel(buff);

			Map<GuildBuff.SubBuff, Double> subBuffs = new LinkedHashMap<>();
			for(int level = 1; level < 9; level++) {
				int slot = startingSlot + level;
				UnlockStatus status = UnlockStatus.LOCKED;
				if(level <= buffLevel) status = UnlockStatus.UNLOCKED;
				if(level - 1 == buffLevel) status = UnlockStatus.UNLOCKING;

				for(Map.Entry<GuildBuff.SubBuff, Double> entry : buff.subBuffMap.get(level).entrySet())
					subBuffs.put(entry.getKey(), subBuffs.getOrDefault(entry.getKey(), 0.0) + entry.getValue());

				DummyItemStack itemStack = getDisplayLevel(buff, level, status, subBuffs);
				getInventory().put(slot, itemStack);
			}
			DummyItemStack itemStack = buff.getDisplayItem(menuGUI.guild, buffLevel);
			List<String> lore = itemStack.getLore();
			if(lore == null) lore = new ArrayList<>();
			ALoreBuilder loreBuilder = new ALoreBuilder(lore);
			for(String line : buff.description) loreBuilder.getLore().add(0, line);
			loreBuilder.addLore("", "&eClick to reset!");
			new AItemStackBuilder(itemStack).setLore(loreBuilder);
			getInventory().put(startingSlot, itemStack);
		}
//		updateInventory();
	}

	public DummyItemStack getDisplayLevel(GuildBuff buff, int level, UnlockStatus status, Map<GuildBuff.SubBuff, Double> subBuffs) {
		ALoreBuilder lore = new ALoreBuilder();
		if(status == UnlockStatus.UNLOCKED) {

		} else if(status == UnlockStatus.UNLOCKING) {

		} else {

		}
		lore.addLore("&7Total Effects");
		for(Map.Entry<GuildBuff.SubBuff, Double> entry : subBuffs.entrySet()) {
			lore.addLore(status.color + entry.getKey().getDisplayString(entry.getValue()));
		}
		if(status == UnlockStatus.UNLOCKED) {
			lore.addLore("", "&7Reputation Cost: &e" + (level + buff.firstLevelCost - 1),
					"&7Points Allocated: &e" + ArcticGuilds.decimalFormat.format(menuGUI.guild.getTotalBuffCost())
							+ "&7/&e" + ArcticGuilds.decimalFormat.format(menuGUI.guild.getRepPoints())
					, "", "&aUnlocked!");
		} else if(status == UnlockStatus.UNLOCKING) {
			lore.addLore("", "&7Reputation Cost: &e" + (level + buff.firstLevelCost - 1),
					"&7Points Allocated: &e" + ArcticGuilds.decimalFormat.format(menuGUI.guild.getTotalBuffCost())
							+ "&7/&e" + ArcticGuilds.decimalFormat.format(menuGUI.guild.getRepPoints())
					, "", "&eClick to unlock!");
		} else {
			lore.addLore("", "&7Reputation Cost: &e" + (level + buff.firstLevelCost - 1),
					"&7Points Allocated: &e" + ArcticGuilds.decimalFormat.format(menuGUI.guild.getTotalBuffCost())
							+ "&7/&e" + ArcticGuilds.decimalFormat.format(menuGUI.guild.getRepPoints())
					, "", "&cUnlock prior levels first!");
		}

		DummyItemStack itemStack = new AItemStackBuilder(status.material, level, status.data)
				.setName("&7Level " + level + " - " + status.color + status.name)
				.setLore(lore)
				.getItemStack();
		return itemStack;
	}

	private enum UnlockStatus {
		UNLOCKED("Unlocked", "STAINED_GLASS_PANE", (short) 5, ChatColor.GREEN),
		UNLOCKING("Click to Unlock", "STAINED_GLASS_PANE", (short) 4, ChatColor.YELLOW),
		LOCKED("Locked", "STAINED_GLASS_PANE", (short) 14, ChatColor.RED);

		public String name;
		public String material;
		public short data;
		public ChatColor color;

		UnlockStatus(String name, String material, short data, ChatColor color) {
			this.name = name;
			this.material = material;
			this.data = data;
			this.color = color;
		}
	}
}
