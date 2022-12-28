package dev.wiji.instancemanager.guilds.inventories;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.guilds.ArcticGuilds;
import dev.wiji.instancemanager.guilds.controllers.PermissionManager;
import dev.wiji.instancemanager.guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.guilds.controllers.objects.GuildMember;
import dev.wiji.instancemanager.guilds.controllers.objects.GuildMemberInfo;
import dev.wiji.instancemanager.guilds.enums.DyeColor;
import dev.wiji.instancemanager.guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.guilds.events.InventoryOpenEvent;
import dev.wiji.instancemanager.misc.*;
import net.md_5.bungee.api.ChatColor;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

public class MenuPanel extends PreparedInventoryPanel {
	public MenuGUI menuGUI;

	public MenuPanel(PreparedGUI gui) {
		super(gui);
		this.menuGUI = (MenuGUI) gui;

		inventoryBuilder.createBorder("STAINED_GLASS_PANE", 7, getRows() * 9)
				.setSlots("STAINED_GLASS_PANE", 7, 11);

		DummyItemStack buffs = new AItemStackBuilder("BEACON")
				.setName("&b&lGuild Buffs")
				.setLore(new ALoreBuilder(

				))
				.getItemStack();
		getInventory().put(12, buffs);

		DummyItemStack upgrades = new AItemStackBuilder("ANVIL")
				.setName("&2&lGuild Upgrades")
				.setLore(new ALoreBuilder(

				))
				.getItemStack();
		getInventory().put(13, upgrades);

//		DummyItemStack shop = new ADummyItemStackBuilder(Material.DOUBLE_PLANT)
//				.setName("&e&lGuild Shop")
//				.setLore(new ALoreBuilder(
//
//				))
//				.getDummyItemStack();
//		getInventory().setItem(14, shop);
//
//		DummyItemStack settings = new ADummyItemStackBuilder(Material.REDSTONE_COMPARATOR)
//				.setName("&f&lGuild Settings")
//				.setLore(new ALoreBuilder(
//
//				))
//				.getDummyItemStack();
//		getInventory().setItem(15, settings);

		setInventory();
	}

	@Override
	public String getName() {
		return ChatColor.GRAY + ((MenuGUI) gui).guild.name + " Guild Menu";
	}

	@Override
	public int getRows() {
		return 3;
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		if(!event.getPlayer().getUniqueId().equals(player.getUniqueId()) || !event.getInventoryName().equals(getName())) {
			return;
		}

		int slot = event.getSlot();
		if(slot == 10) {
			Map.Entry<GuildMember, GuildMemberInfo> entry = menuGUI.guild.getMember(player);
			if(!PermissionManager.isAdmin(player)) {
				if(!entry.getValue().rank.isAtLeast(Constants.BANNER_COLOR_PERMISSION)) {
					AOutput.error(player, "You must be at least " + Constants.BANNER_COLOR_PERMISSION.displayName + " to do this");
					return;
				}
			}
			openPanel(menuGUI.dyePanel);
		} else if(slot == 12) {
			openPanel(menuGUI.buffPanel);
		} else if(slot == 13) {
			openPanel(menuGUI.upgradePanel);
		} else if(slot == 14) {
//			openPanel(menuGUI.shopPanel);
//			Sounds.NO.play(player);
//			AOutput.error(player, "This feature is still being developed");
		} else if(slot == 15) {
//			openPanel(menuGUI.settingsPanel);
//			Sounds.NO.play(player);
//			AOutput.error(player, "This feature is still being developed");
		}
	}

	@Override
	public void onOpen(InventoryOpenEvent event) {}

	@Override
	public void onClose(InventoryCloseEvent event) {
//		PreparedInventoryPanel.panels.remove(this);
	}

	public void setInventory() {
		DyeColor dyeColor = null;
		for(DyeColor value : DyeColor.values()) {
			if(value.getDyeData() != menuGUI.guild.bannerColor) continue;
			dyeColor = value;
		}
		assert dyeColor != null;

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		dateFormat.setTimeZone(TimeZone.getTimeZone(ConfigManager.configuration.getString("timezone")));

		DummyItemStack stats = new DummyItemStack("BANNER");
		stats.addModifier("BANNER_COLOR:" + dyeColor.getDyeData());

		ChatColor chatColor = ColorConverter.getChatColor(dyeColor);
		new AItemStackBuilder(stats)
				.setName(chatColor + "&l" + menuGUI.guild.name + " Information")
				.setLore(new ALoreBuilder(
						"&7Date Created: " + chatColor + dateFormat.format(menuGUI.guild.dateCreated),
						"&7Owner: " + BungeeMain.getName(menuGUI.guild.ownerUUID, false),
						"&7Members: " + chatColor + menuGUI.guild.members.size(),
						"&7Guild Rank: " + menuGUI.guild.getFormattedRank(),
						"&7Reputation: " + chatColor + ArcticGuilds.decimalFormat.format(menuGUI.guild.reputation),
						"&7Balance: &6" + menuGUI.guild.getFormattedBalance() + "g",
						"",
						"&7Click to change dye color"
				));
		getInventory().put(10, stats);
	}
}
