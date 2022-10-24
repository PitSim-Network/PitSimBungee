package dev.wiji.instancemanager.Guilds;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Misc.AUtil;

import java.text.DecimalFormat;

public class ArcticGuilds {
	public static BungeeMain INSTANCE;

	public static DecimalFormat decimalFormat = new DecimalFormat("#,##0");


	public static void onEnable(BungeeMain instance) {
		INSTANCE = instance;

		registerCommands();
		registerBuffs();
		registerUpgrades();
		registerListeners();
	}


	public static void onDisable(BungeeMain instance) {

	}

	private static void registerCommands() {
//		GuildCommand guildCommand = new GuildCommand("guild");
//		new HelpCommand(guildCommand, "help");
//		new InfoCommand(guildCommand, "info");
//		new CreateCommand(guildCommand, "create");
//		new TransferCommand(guildCommand, "transfer");
//		new DisbandCommand(guildCommand, "disband");
//		new ChatCommand(guildCommand, "chat");
//		new MenuCommand(guildCommand, "menu");
//		new TagCommand(guildCommand, "tag");
//		new RenameCommand(guildCommand, "rename");
//
//		new InviteCommand(guildCommand, "invite");
//		new JoinCommand(guildCommand, "join");
//		new PromoteCommand(guildCommand, "promote");
//		new DemoteCommand(guildCommand, "demote");
//		new LeaveCommand(guildCommand, "leave");
//		new KickCommand(guildCommand, "kick");
//
//		new BalanceCommand(guildCommand, "bal");
//		new DepositCommand(guildCommand, "deposit");
//		new WithdrawalCommand(guildCommand, "withdrawal");
//
//		GuildAdminCommand adminCommand = new GuildAdminCommand("gadmin");
//		new ReputationCommand(adminCommand, "rep");
	}

	private static void registerListeners() {
//		getServer().getPluginManager().registerEvents(new ChatManager(), this);
//		getServer().getPluginManager().registerEvents(new BuffManager(), this);
//		getServer().getPluginManager().registerEvents(new UpgradeManager(), this);
//		getServer().getPluginManager().registerEvents(new GuildManager(), this);
//		getServer().getPluginManager().registerEvents(new PlayerManager(), this);
//		getServer().getPluginManager().registerEvents(new PermissionManager(), this);
	}

	private static void registerBuffs() {
//		BuffManager.registerBuff(new DamageBuff());
//		BuffManager.registerBuff(new DefenceBuff());
//		BuffManager.registerBuff(new XPBuff());
//		BuffManager.registerBuff(new GoldBuff());
////		BuffManager.registerBuff(new DispersionBuff());
//		BuffManager.registerBuff(new RenownBuff());
	}

	private static void registerUpgrades() {
//		UpgradeManager.registerUpgrade(new GuildSize());
//		UpgradeManager.registerUpgrade(new BankLimit());
//		UpgradeManager.registerUpgrade(new ReputationIncrease());
//		UpgradeManager.registerUpgrade(new GuildBuffs());
	}
}
