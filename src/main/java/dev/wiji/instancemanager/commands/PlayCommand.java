package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.MainGamemodeServer;
import dev.wiji.instancemanager.objects.OverworldServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.objects.ServerStatus;
import dev.wiji.instancemanager.pitsim.CommandListener;
import dev.wiji.instancemanager.pitsim.OverworldServerManager;
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
import org.apache.log4j.chainsaw.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayCommand extends Command {
	public PlayCommand(Plugin bungeeMain) {
		super("play");
	}

	public static List<ProxiedPlayer> queuingPlayers = new ArrayList<>();

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

				if(MainGamemodeServer.cooldownPlayers.containsKey(player)) {
					long time = MainGamemodeServer.cooldownPlayers.get(player);

					if(time + CommandListener.COOLDOWN_SECONDS * 1000 < System.currentTimeMillis()) {
						MainGamemodeServer.cooldownPlayers.remove(player);
					}
				}

				if(MainGamemodeServer.guildCooldown.containsKey(player)) {
					long time = MainGamemodeServer.guildCooldown.get(player);

					if(time + CommandListener.COOLDOWN_SECONDS * 1000 < System.currentTimeMillis()) {
						MainGamemodeServer.guildCooldown.remove(player);
					}
				}

				if(MainGamemodeServer.guildCooldown.containsKey(player) || MainGamemodeServer.cooldownPlayers.containsKey(player) || ServerChangeListener.recentlyLeft.contains(player)) {
					if(queuingPlayers.contains(player)) return;
					AOutput.color(player, "&eQueuing you to find a server!");
					queuingPlayers.add(player);
					((ProxyRunnable) () -> execute(commandSender, strings)).runAfter(3, TimeUnit.SECONDS);
					return;
				}

				commandSender.sendMessage((new ComponentBuilder("Looking for a server...").color(ChatColor.GREEN).create()));
				queuingPlayers.remove(player);

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
