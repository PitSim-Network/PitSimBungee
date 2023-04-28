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


	}
}
