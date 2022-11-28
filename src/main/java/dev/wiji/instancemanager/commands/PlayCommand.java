package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.objects.PitSimServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.objects.ServerStatus;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.skywars.SkywarsGameManager;
import dev.wiji.instancemanager.skywars.SkywarsQueueManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayCommand extends Command {
	public PlayCommand(Plugin bungeeMain) {
		super("play");
	}

	public static List<ProxiedPlayer> cooldownPlayers = new ArrayList<>();

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if(!(commandSender instanceof ProxiedPlayer)) return;

		if(strings.length != 1) {
			commandSender.sendMessage((new ComponentBuilder("Invalid args!").color(ChatColor.RED).create()));
			return;
		}

		ProxiedPlayer player = ((ProxiedPlayer) commandSender);

		if(cooldownPlayers.contains(player)) {
			player.sendMessage((new ComponentBuilder("This command is on cooldown!").color(ChatColor.RED).create()));
			return;
		}

		cooldownPlayers.add(player);
		((ProxyRunnable) () -> cooldownPlayers.remove(player)).runAfter(5, TimeUnit.SECONDS);

		if(strings[0].equalsIgnoreCase("pitsim")) {


			Server currentServer = player.getServer();

			if(currentServer.getInfo().getName().contains("pitsim")) {
				boolean canChange = false;
				for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
					if(pitSimServer.getServerInfo() == currentServer.getInfo()) continue;
					if(pitSimServer.status != ServerStatus.RUNNING) continue;
					canChange = true;
				}

				if(!canChange) {
					commandSender.sendMessage((new ComponentBuilder("There are currently no other available servers!").color(ChatColor.RED).create()));
					return;
				}

				commandSender.sendMessage((new ComponentBuilder("Looking for a server...").color(ChatColor.GREEN).create()));
				new PluginMessage().writeString("SAVE DATA").writeString(player.getName()).addServer(currentServer.getInfo()).send();
				return;

			}

			PitSimServerManager.queue((ProxiedPlayer) commandSender, 0, false);

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
