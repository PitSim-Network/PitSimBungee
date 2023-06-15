package net.pitsim.bungee.commands;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.objects.PitSimServer;
import net.pitsim.bungee.objects.PluginMessage;
import net.pitsim.bungee.objects.ServerStatus;
import net.pitsim.bungee.objects.ServerType;
import net.pitsim.bungee.pitsim.PitSimServerManager;
import net.pitsim.bungee.skywars.SkywarsGameManager;
import net.pitsim.bungee.skywars.SkywarsQueueManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class PlayCommand extends Command {

	public PlayCommand(Plugin bungeeMain) {
		super("play");
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if(!(commandSender instanceof ProxiedPlayer)) return;

		ProxiedPlayer player = ((ProxiedPlayer) commandSender);

		if(strings.length < 1 || strings[0].toLowerCase().startsWith("pit")) {
			Server currentServer = player.getServer();
			PitSimServerManager manager = PitSimServerManager.getManager(ServerType.OVERWORLD);
			assert manager != null;

			if(currentServer.getInfo().getName().contains("pitsim") || currentServer.getInfo().getName().contains("darkzone")) {
				boolean canChange = false;

				for(PitSimServer overworldServer : manager.serverList) {
					if(overworldServer.getServerInfo() == currentServer.getInfo()) continue;
					if(overworldServer.status != ServerStatus.RUNNING) continue;
					canChange = true;
				}

				if(!canChange) {
					commandSender.sendMessage((new ComponentBuilder("There are currently no other available servers!").color(ChatColor.RED).create()));
					return;
				}

				commandSender.sendMessage((new ComponentBuilder("Looking for a server...").color(ChatColor.GREEN).create()));

				new PluginMessage().writeString("REQUEST SWITCH").writeString(player.getUniqueId().toString()).addServer(currentServer.getInfo()).send();
				return;
			}


			manager.queueFallback((ProxiedPlayer) commandSender, 0, false);

		} else if(strings[0].equalsIgnoreCase("sync")) {
			commandSender.sendMessage((new ComponentBuilder("Sending you to Sync!").color(ChatColor.GREEN).create()));
			((ProxiedPlayer) commandSender).connect(BungeeMain.INSTANCE.getProxy().getServerInfo("sync"));
		} else if(strings[0].equalsIgnoreCase("skywars")) {
			if(SkywarsGameManager.isEnabled) {
				commandSender.sendMessage((new ComponentBuilder("Sending you to Skywars!").color(ChatColor.GREEN).create()));
				SkywarsQueueManager.queue((ProxiedPlayer) commandSender);
			} else
				commandSender.sendMessage((new ComponentBuilder("Queuing for this gamemode is currently disabled.").color(ChatColor.RED).create()));
		} else if(strings[0].equalsIgnoreCase("rewind")) {
			commandSender.sendMessage((new ComponentBuilder("Sending you to Rewind!").color(ChatColor.GREEN).create()));
			((ProxiedPlayer) commandSender).connect(BungeeMain.INSTANCE.getProxy().getServerInfo("rewind"));
		}
	}
}
