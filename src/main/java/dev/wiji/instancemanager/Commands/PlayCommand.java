package dev.wiji.instancemanager.Commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.PitSim.PitSimServerManager;
import dev.wiji.instancemanager.Skywars.SkywarsGameManager;
import dev.wiji.instancemanager.Skywars.SkywarsQueueManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class PlayCommand extends Command {
	public PlayCommand(Plugin bungeeMain) {
		super("play");
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if(!(commandSender instanceof ProxiedPlayer)) return;

		if(strings.length != 1) {
			commandSender.sendMessage((new ComponentBuilder("Invalid args!").color(ChatColor.RED).create()));
			return;
		}

		if(strings[0].equalsIgnoreCase("pitsim")) {
			if(!PitSimServerManager.queue((ProxiedPlayer) commandSender, 0)) {
				commandSender.sendMessage((new ComponentBuilder("There are currently no available servers!").color(ChatColor.RED).create()));
			}
		} else if(strings[0].equalsIgnoreCase("sync")) {
			commandSender.sendMessage((new ComponentBuilder("Sending you to Sync!").color(ChatColor.GREEN).create()));
		((ProxiedPlayer) commandSender).connect(BungeeMain.INSTANCE.getProxy().getServerInfo("sync"));
		} else if(strings[0].equalsIgnoreCase("skywars")) {
			if(SkywarsGameManager.isEnabled) {
				commandSender.sendMessage((new ComponentBuilder("Sending you to Skywars!").color(ChatColor.GREEN).create()));
				SkywarsQueueManager.queue((ProxiedPlayer) commandSender);
			} else commandSender.sendMessage((new ComponentBuilder("Queuing for this gamemode is currently disabled.").color(ChatColor.RED).create()));
		} else if(strings[0].equalsIgnoreCase("rewind")) {
				commandSender.sendMessage((new ComponentBuilder("Sending you to Rewind!").color(ChatColor.GREEN).create()));
				((ProxiedPlayer) commandSender).connect(BungeeMain.INSTANCE.getProxy().getServerInfo("rewind"));
		}
	}
}
