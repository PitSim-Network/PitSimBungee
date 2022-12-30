package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.objects.MainGamemodeServer;
import dev.wiji.instancemanager.objects.OverworldServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.objects.ServerStatus;
import dev.wiji.instancemanager.pitsim.OverworldServerManager;
import dev.wiji.instancemanager.skywars.SkywarsGameManager;
import dev.wiji.instancemanager.skywars.SkywarsQueueManager;
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

			if(currentServer.getInfo().getName().contains("pitsim") || currentServer.getInfo().getName().contains("darkzone")) {
				boolean canChange = false;
				for(OverworldServer overworldServer : OverworldServerManager.serverList) {
					if(overworldServer.getServerInfo() == currentServer.getInfo()) continue;
					if(overworldServer.status != ServerStatus.RUNNING) continue;
					canChange = true;
				}

				if(!canChange) {
					commandSender.sendMessage((new ComponentBuilder("There are currently no other available servers!").color(ChatColor.RED).create()));
					return;
				}

				System.out.println("SWITCH FROM: " + currentServer);
				commandSender.sendMessage((new ComponentBuilder("Looking for a server...").color(ChatColor.GREEN).create()));

				MainGamemodeServer.guildCooldown.put(player, System.currentTimeMillis());
				new PluginMessage().writeString("REQUEST SWITCH").writeString(player.getUniqueId().toString()).addServer(currentServer.getInfo()).send();
				return;

			}

			OverworldServerManager.queueFallback((ProxiedPlayer) commandSender, 0, false);

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
