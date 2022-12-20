package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.DarkzoneServer;
import dev.wiji.instancemanager.objects.OverworldServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.pitsim.DarkzoneServerManager;
import dev.wiji.instancemanager.pitsim.OverworldServerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.List;

public class ServerCommand extends Command {
	public ServerCommand() {
		super("join");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		if(!player.hasPermission("pitsim.join")) return;

		if(args.length < 1) {
			sendServerList(player);
			return;
		}

		ServerInfo previousServer = player.getServer().getInfo();

		String requestedServerString = args[0];
		ServerInfo requestedServer = BungeeMain.INSTANCE.getProxy().getServerInfo(requestedServerString);

		if(requestedServer == null) {
			AOutput.error(player, "That server does not exist! Run /join for a list of servers");
			return;
		}

		for(OverworldServer overworldServer : OverworldServerManager.serverList) {
			if(overworldServer.getServerInfo() == requestedServer) {
				if(previousServer.getName().contains("darkzone") || previousServer.getName().contains("pitsim")) {
					new PluginMessage().writeString("REQUEST SWITCH").writeString(player.getUniqueId().toString())
							.writeInt(overworldServer.getServerIndex()).addServer(previousServer).send();
					return;
				}

				OverworldServerManager.queue(player, overworldServer.getServerIndex(), false);
				return;
			}
		}

		for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
			if(darkzoneServer.getServerInfo() == requestedServer) {
				if(previousServer.getName().contains("darkzone") || previousServer.getName().contains("pitsim")) {
					new PluginMessage().writeString("REQUEST DARKZONE SWITCH").writeString(player.getUniqueId().toString())
							.writeInt(darkzoneServer.getServerIndex()).addServer(previousServer).send();
					return;
				}

				DarkzoneServerManager.queue(player, darkzoneServer.getServerIndex());
				return;
			}
		}

		AOutput.color(player, "&2&lSERVERS &aSent you to " + requestedServer.getName());
		if(requestedServer.getName().contains("pitsim") || requestedServer.getName().contains("darkzone")) {
			AOutput.error(player, "You are unable to connect to this server at this time.");
			return;
		}
		player.connect(requestedServer);
	}

	public void sendServerList(ProxiedPlayer player) {
		List<ServerInfo> servers = new ArrayList<>(BungeeMain.INSTANCE.getProxy().getServers().values());

		AOutput.color(player, "&2&lSERVERS &6List of servers:");
		AOutput.color(player, "&7&m-----------------------");

		for(ServerInfo server : servers) {
			TextComponent textComponent = new TextComponent(server.getName());
			textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + server.getName()));
			textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(server.getPlayers().size() + " players")));
			textComponent.setColor(ChatColor.GOLD);

			if(server == player.getServer().getInfo()) {
				textComponent.addExtra(ChatColor.DARK_GRAY + " (current)");
			}

			player.sendMessage(textComponent);
		}

		AOutput.color(player, "&7&m-----------------------");
	}
}
