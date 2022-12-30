package dev.wiji.instancemanager.guilds;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.guilds.commands.admin.GuildAdminCommand;
import dev.wiji.instancemanager.guilds.commands.admin.ReputationCommand;
import dev.wiji.instancemanager.guilds.commands.guildcommands.*;
import dev.wiji.instancemanager.guilds.controllers.*;
import dev.wiji.instancemanager.guilds.guildbuffs.*;
import dev.wiji.instancemanager.guilds.guildupgrades.BankLimit;
import dev.wiji.instancemanager.guilds.guildupgrades.GuildBuffs;
import dev.wiji.instancemanager.guilds.guildupgrades.GuildSize;
import dev.wiji.instancemanager.guilds.guildupgrades.ReputationIncrease;
import dev.wiji.instancemanager.misc.AMultiCommand;
import dev.wiji.instancemanager.misc.APlayerData;
import dev.wiji.instancemanager.misc.InventoryManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ArcticGuilds {
	public static BungeeMain INSTANCE;

	public static DecimalFormat decimalFormat = new DecimalFormat("#,##0");


	public static void onEnable(BungeeMain instance) {
		INSTANCE = instance;
		APlayerData.init();

		registerCommands();
		registerBuffs();
		registerUpgrades();
		registerListeners();
	}


	public static void onDisable(BungeeMain instance) {

	}

	private static void registerCommands() {

		List<AMultiCommand> guildCommands = new ArrayList<>();
		guildCommands.add(new GuildCommand("guild"));
		guildCommands.add(new GuildCommand("g"));

		for(AMultiCommand guildCommand : guildCommands) {
			new HelpCommand(guildCommand, "help");
			new InfoCommand(guildCommand, "info");
			new CreateCommand(guildCommand, "create");
			new TransferCommand(guildCommand, "transfer");
			new DisbandCommand(guildCommand, "disband");
			new ChatCommand(guildCommand, "chat");
			new MenuCommand(guildCommand, "menu");
			new TagCommand(guildCommand, "tag");
			new RenameCommand(guildCommand, "rename");
//
			new InviteCommand(guildCommand, "invite");
			new JoinCommand(guildCommand, "join");
			new PromoteCommand(guildCommand, "promote");
			new DemoteCommand(guildCommand, "demote");
			new LeaveCommand(guildCommand, "leave");
			new KickCommand(guildCommand, "kick");
//
			new BalanceCommand(guildCommand, "bal");
			new DepositCommand(guildCommand, "deposit");
			new WithdrawCommand(guildCommand, "withdraw");
		}
//
		GuildAdminCommand adminCommand = new GuildAdminCommand("gadmin");
		new ReputationCommand(adminCommand, "rep");
	}

	private static void registerListeners() {
		INSTANCE.getProxy().getPluginManager().registerListener(INSTANCE, new ChatManager());
		INSTANCE.getProxy().getPluginManager().registerListener(INSTANCE, new BuffManager());
		INSTANCE.getProxy().getPluginManager().registerListener(INSTANCE, new UpgradeManager());
		INSTANCE.getProxy().getPluginManager().registerListener(INSTANCE, new GuildManager());
		INSTANCE.getProxy().getPluginManager().registerListener(INSTANCE, new PermissionManager());
		INSTANCE.getProxy().getPluginManager().registerListener(INSTANCE, new GuildMessaging());
		INSTANCE.getProxy().getPluginManager().registerListener(INSTANCE, new InventoryManager());
	}

	private static void registerBuffs() {
		BuffManager.registerBuff(new DamageBuff());
		BuffManager.registerBuff(new DefenceBuff());
		BuffManager.registerBuff(new XPBuff());
		BuffManager.registerBuff(new GoldBuff());
//		BuffManager.registerBuff(new DispersionBuff());
		BuffManager.registerBuff(new RenownBuff());
	}

	private static void registerUpgrades() {
		UpgradeManager.registerUpgrade(new GuildSize());
		UpgradeManager.registerUpgrade(new BankLimit());
		UpgradeManager.registerUpgrade(new ReputationIncrease());
		UpgradeManager.registerUpgrade(new GuildBuffs());
	}
}
