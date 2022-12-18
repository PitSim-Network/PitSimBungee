package dev.wiji.instancemanager.discord;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.pitsim.ServerDataManager;
import net.luckperms.api.LuckPerms;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DiscordPlugin {
	public static BungeeMain INSTANCE;

	public static LuckPerms LUCKPERMS;

	public static void onEnable(BungeeMain instance) {
		INSTANCE = instance;
		LUCKPERMS = BungeeMain.LUCKPERMS;
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);

		new DiscordManager();

		DiscordManager.registerCommand(new PingCommand());
//		DiscordManager.registerCommand(new VerifyCommand());

		instance.getProxy().getPluginManager().registerListener(instance, new InGameNitro());
	}

	public static void onDisable() {

		DiscordManager.disable();
	}
}
