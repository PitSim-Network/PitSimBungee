package dev.wiji.instancemanager.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class DevCommand extends Command {
	public DevCommand(Plugin bungeeMain) {
		super("dev");
	}

	public static boolean isEnabled = false;

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if(!(commandSender instanceof ProxiedPlayer)) return;
//
//		if(!isEnabled && !commandSender.hasPermission("pitsim.toggle")) {
//			commandSender.sendMessage((new ComponentBuilder("Access to this server is currently disabled!").color(ChatColor.RED).create()));
//			return;
//		}
//
//		((ProxiedPlayer) commandSender).connect(BungeeMain.INSTANCE.getProxy().getServerInfo("dev2"));
	}
}
