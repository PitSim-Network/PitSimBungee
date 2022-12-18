package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.objects.MainServer;
import dev.wiji.instancemanager.storage.StorageManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class PTestCommand extends Command {
	public PTestCommand(Plugin bungeeMain) {
		super("ptest");
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
//		if(!(commandSender instanceof ProxiedPlayer)) return;
//		if(commandSender instanceof ProxiedPlayer) return;
//		if(!player.hasPermission("pitsim.admin")) return;

		System.out.println("------------------------");
		for(MainServer mainServer : MainServer.serverList) {
			System.out.println("all servers: " + mainServer.getServerInfo().getName() + " " + mainServer.getLoadedProfiles());
		}
		System.out.println(">>>>>>>>>><<<<<<<<<<");
		System.out.println(StorageManager.profiles.toString());
		System.out.println("------------------------");
	}
}
