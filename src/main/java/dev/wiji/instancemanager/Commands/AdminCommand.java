package dev.wiji.instancemanager.Commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.Objects.DarkzoneServer;
import dev.wiji.instancemanager.Objects.PitSimServer;
import dev.wiji.instancemanager.Objects.PluginMessage;
import dev.wiji.instancemanager.Objects.ServerStatus;
import dev.wiji.instancemanager.PitSim.DarkzoneServerManager;
import dev.wiji.instancemanager.PitSim.PitSimServerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class AdminCommand extends Command {
	public AdminCommand(Plugin bungeeMain) {
		super("admin");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {

		if(!(sender instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) sender;

		if(!player.hasPermission("pitsim.admin")) return;


		if(args.length < 1) {
			player.sendMessage(new ComponentBuilder("Usage: /admin <status|shutdown|stopnetwork|startnetwork|killnetwork>").color(ChatColor.RED).create());
			return;
		}

		if(args[0].equalsIgnoreCase("shutdown")) {

			if(args.length != 2) {
				player.sendMessage((new ComponentBuilder("Invalid arguments! /admin shutdown <minutes>").color(ChatColor.RED).create()));
			}

			int minutes = 0;

			try {
				minutes = Integer.parseInt(args[1]);
			} catch(Exception e) {
				player.sendMessage((new ComponentBuilder("Invalid arguments! /admin shutdown <minutes>").color(ChatColor.RED).create()));
				return;
			}

			if(minutes < 1) {
				player.sendMessage((new ComponentBuilder("Invalid arguments! /admin shutdown <minutes>").color(ChatColor.RED).create()));
				return;
			}

			Server server = player.getServer();
			for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
				if(server.getInfo() == pitSimServer.getServerInfo()) {
					pitSimServer.status = ServerStatus.SHUTTING_DOWN_INITIAL;
					new PluginMessage().writeString("SHUTDOWN").writeBoolean(false).writeInt(minutes).addServer(pitSimServer.getServerInfo()).send();
				}
			}

			for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
				if(server.getInfo() == darkzoneServer.getServerInfo()) {
					darkzoneServer.status = ServerStatus.SHUTTING_DOWN_INITIAL;
					new PluginMessage().writeString("SHUTDOWN").writeBoolean(false).writeInt(minutes).addServer(darkzoneServer.getServerInfo()).send();
				}
			}
		}

		if(args[0].equalsIgnoreCase("stopnetwork")) {

			if(PitSimServerManager.networkIsShuttingDown || DarkzoneServerManager.networkIsShuttingDown) {
				player.sendMessage((new ComponentBuilder("Network is already shutting down!").color(ChatColor.RED).create()));
				return;
			}

			PitSimServerManager.networkIsShuttingDown = true;
			DarkzoneServerManager.networkIsShuttingDown = true;

			Server server = player.getServer();
			for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
				if(pitSimServer.status == ServerStatus.RUNNING) {
					pitSimServer.shutDown(false);
				}
				if(pitSimServer.status == ServerStatus.STARTING) {
					pitSimServer.hardShutDown();
				}
			}

			for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
				if(darkzoneServer.status == ServerStatus.RUNNING) {
					darkzoneServer.shutDown(false);
				}
				if(darkzoneServer.status == ServerStatus.STARTING) {
					darkzoneServer.hardShutDown();
				}
			}

			player.sendMessage((new ComponentBuilder("Initiated network shutdown!").color(ChatColor.GREEN).create()));
		}

		if(args[0].equalsIgnoreCase("killnetwork")) {

			PitSimServerManager.networkIsShuttingDown = true;
			DarkzoneServerManager.networkIsShuttingDown = true;

			Server server = player.getServer();
			for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
				for(ProxiedPlayer pitSimServerPlayer : pitSimServer.getPlayers()) {
					pitSimServerPlayer.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
				}

				pitSimServer.hardShutDown();
			}

			for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
				if(server.getInfo() == darkzoneServer.getServerInfo()) {
					for(ProxiedPlayer darkzoneServerPlayer : darkzoneServer.getPlayers()) {
						darkzoneServerPlayer.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
					}

					darkzoneServer.hardShutDown();
				}
			}

			player.sendMessage((new ComponentBuilder("Initiated immediate network shutdown!").color(ChatColor.GREEN).create()));
		}

		if(args[0].equalsIgnoreCase("startnetwork")) {

			if(!PitSimServerManager.networkIsShuttingDown || !DarkzoneServerManager.networkIsShuttingDown) {
				player.sendMessage((new ComponentBuilder("The network isn't shut down!").color(ChatColor.RED).create()));
				return;
			}

			PitSimServerManager.networkIsShuttingDown = false;
			DarkzoneServerManager.networkIsShuttingDown = false;

			player.sendMessage((new ComponentBuilder("Resumed network processes!").color(ChatColor.GREEN).create()));
		}

		if(args[0].equalsIgnoreCase("status")) {

			player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&8&m-------------------------------")));

			for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
				BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
						"&7[" + pitSimServer.getServerInfo().getName() + "] &e(" + pitSimServer.getPlayers().size() + ") "  + pitSimServer.status.color +
								pitSimServer.status.toString()));

				player.sendMessage(components);
			}

			for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
				BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
						"&7[" + darkzoneServer.getServerInfo().getName() + "] &e(" + darkzoneServer.getPlayers().size() + ") " + darkzoneServer.status.color +
								darkzoneServer.status.toString()));

				player.sendMessage(components);
			}

			player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&8&m-------------------------------")));

		}
	}
}
