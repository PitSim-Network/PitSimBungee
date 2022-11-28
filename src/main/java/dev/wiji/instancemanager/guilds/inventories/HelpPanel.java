package dev.wiji.instancemanager.guilds.inventories;

import dev.wiji.instancemanager.guilds.controllers.objects.DummyItemStack;
import dev.wiji.instancemanager.guilds.events.InventoryClickEvent;
import dev.wiji.instancemanager.guilds.events.InventoryCloseEvent;
import dev.wiji.instancemanager.guilds.events.InventoryOpenEvent;
import dev.wiji.instancemanager.misc.*;
import net.md_5.bungee.api.ChatColor;

public class HelpPanel extends PreparedInventoryPanel {
	public HelpGUI helpGUI;

	public HelpPanel(PreparedGUI gui) {
		super(gui);
		this.helpGUI = (HelpGUI) gui;

		inventoryBuilder.createBorder("STAINED_GLASS_PANE", 7, getRows() * 9);

		DummyItemStack general = new AItemStackBuilder("PAPER")
				.setName("&f&lWhat are Guilds?")
				.setLore(new ALoreBuilder(
						"&7Guilds allow players to group up",
						"&7to fight, streak, and hang out",
						"",
						"&7Being a part of a guild comes with"
				))
				.getItemStack();
		getInventory().put(10, general);

		DummyItemStack reputation = new AItemStackBuilder("DIAMOND")
				.setName("&b&lReputation System")
				.setLore(new ALoreBuilder(
						"&7Guild reputation measures the strength",
						"&7of a guild and can be earned from a",
						"&7variety of tasks",
						"",
						"&7Each 1,000 reputation gives a reputation point",
						"&7which can be used to activate specific buffs",
						"",
						"&7Reputation Sources:",
						"&7 * Being active",
						"&7 * Killing other guilds",
						"&7 * Making other guilds lose feathers",
						"&7 * Streaking (WIP--NOT IMPLEMENTED)"
				))
				.getItemStack();
		getInventory().put(11, reputation);

		DummyItemStack upgrades = new AItemStackBuilder("ANVIL")
				.setName("&2&lUpgrades")
				.setLore(new ALoreBuilder(
						"&7Unlike reputation buffs, upgrades are permanent.",
						"&7Upgrades can be found in the upgrade GUI and improve",
						"&7your guild in many ways",
						"",
						"&7To purchase an upgrade, have a " + Constants.UPGRADES_PERMISSION.displayName + " purchase a upgrade",
						"&7and ensure that there is enough money in the guild bank"
				))
				.getItemStack();
		getInventory().put(12, upgrades);

		DummyItemStack rewards = new AItemStackBuilder("FEATHER")
				.setName("&e&lRewards")
				.setLore(new ALoreBuilder(
						"&7Rewards will most likely be given out",
						"&7in the form of feathers to the best performing",
						"&7guilds (Feature WIP)"
				))
				.getItemStack();
		getInventory().put(13, rewards);

		DummyItemStack joinLeave = new AItemStackBuilder("HOPPER")
				.setName("&7&lJoin/Create Cooldown")
				.setLore(new ALoreBuilder(
						"&7Joining and creating guilds has a cooldown",
						"&7to prevent players from abusing guilds",
						"",
						"&7Reference the /guild command",
						"&7to see commands for these actions"
				))
				.getItemStack();
		getInventory().put(14, joinLeave);
	}

	@Override
	public String getName() {
		return ChatColor.GRAY + "Guild Help";
	}

	@Override
	public int getRows() {
		return 3;
	}

	@Override
	public void onClick(InventoryClickEvent event) { }

	@Override
	public void onOpen(InventoryOpenEvent event) { }

	@Override
	public void onClose(InventoryCloseEvent event) { }
}
