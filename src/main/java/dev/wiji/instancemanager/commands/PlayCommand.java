package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.CommandBlocker;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.PitSimServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.objects.ServerStatus;
import dev.wiji.instancemanager.objects.ServerType;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;
import dev.wiji.instancemanager.pitsim.ServerChangeListener;
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

	public static final int JOINS_PER_SECOND = 5;

	private int currentJoinCount = 0;
	public static List<ProxiedPlayer> queuingPlayers = new ArrayList<>();


	public PlayCommand(Plugin bungeeMain) {
		super("play");

		((ProxyRunnable) () -> currentJoinCount = 0).runAfterEvery(0, 1, TimeUnit.SECONDS);
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if(!(commandSender instanceof ProxiedPlayer)) return;

		ProxiedPlayer player = ((ProxiedPlayer) commandSender);

		if(strings.length < 1 || strings[0].toLowerCase().startsWith("pit")) {
			Server currentServer = player.getServer();
			PitSimServerManager manager = PitSimServerManager.getManager(ServerType.OVERWORLD);
			assert manager != null;

			if(queuingPlayers.contains(player)) {
				AOutput.color(player, "&eYou are already in the queue!");
				return;
			}

			if(currentJoinCount > JOINS_PER_SECOND || CommandBlocker.blockedPlayers.contains(player.getUniqueId()) ||
					ServerChangeListener.recentlyLeft.contains(player.getUniqueId())) {

				System.out.println(1);
				if(queuingPlayers.contains(player)) return;
				System.out.println(2);

				AOutput.color(player, "&eQueuing you to find a server!");
				queuingPlayers.add(player);
				((ProxyRunnable) () -> {
					queuingPlayers.remove(player);
					execute(commandSender, strings);
				}).runAfter(3, TimeUnit.SECONDS);
				return;
			}

			currentJoinCount++;

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

//				CommandBlocker.blockPlayer(player.getUniqueId());
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
