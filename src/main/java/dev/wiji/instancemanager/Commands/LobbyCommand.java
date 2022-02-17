package dev.wiji.instancemanager.Commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Skywars.SkywarsGameManager;
import dev.wiji.instancemanager.Skywars.SkywarsQueueManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class LobbyCommand extends Command {
	public LobbyCommand(Plugin bungeeMain) {
		super("lobby", "", "hub", "l", "LOBBY", "Lobby", "HUB", "L", "Hub");
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if(!(commandSender instanceof ProxiedPlayer)) return;

		ProxiedPlayer player = (ProxiedPlayer) commandSender;

		String server = player.getServer().getInfo().getName();

		if(server.equalsIgnoreCase("lobby")) {
			commandSender.sendMessage((new ComponentBuilder("You are already in the Lobby!").color(ChatColor.GREEN).create()));
			return;
		}

		List<String> skywarsServers = new ArrayList<>(SkywarsGameManager.activeServers.keySet());
		skywarsServers.add(SkywarsGameManager.mainQueueServer);
		skywarsServers.add(SkywarsGameManager.backupQueueServer);

		if(skywarsServers.contains(server)) {
			commandSender.sendMessage((new ComponentBuilder("Sending you to the Skywars Lobby").color(ChatColor.GREEN).create()));
			((ProxiedPlayer) commandSender).connect(BungeeMain.INSTANCE.getProxy().getServerInfo("skywars"));
		} else {
			commandSender.sendMessage((new ComponentBuilder("Sending you to the Lobby").color(ChatColor.GREEN).create()));
			((ProxiedPlayer) commandSender).connect(BungeeMain.INSTANCE.getProxy().getServerInfo("lobby"));
		}
	}
}
