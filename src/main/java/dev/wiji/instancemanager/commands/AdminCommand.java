package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.builders.MessageBuilder;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.DarkzoneServer;
import dev.wiji.instancemanager.objects.PitSimServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.objects.ServerStatus;
import dev.wiji.instancemanager.pitsim.DarkzoneServerManager;
import dev.wiji.instancemanager.pitsim.PitSimServerManager;
import dev.wiji.instancemanager.storage.EditSessionManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
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
			new MessageBuilder(
					"&4&m--------------------&4<&c&lADMIN&4>&m--------------------",
					"&4 * &c/admin status &7(displays the status of the servers)",
					"&4 * &c/admin shutdown &7(shuts down the player's current server)",
					"&4 * &c/admin startnetwork &7(starts up the network)",
					"&4 * &c/admin stopnetwork &7(shuts down the network)",
					"&4 * &c/admin killnetwork &7(shuts down the network immediately)",
					"&4 * &c/admin suspend [server-name] [kick-players?] &7(maintenance mode)",
					"&4 * &c/admin edit &7(editing playerdata)",
					"&4&m--------------------&4<&c&lADMIN&4>&m--------------------"
			).send(player);
			return;
		}

		if(args[0].equalsIgnoreCase("shutdown")) {

			if(args.length != 2) {
				player.sendMessage((new ComponentBuilder("Invalid arguments! /admin shutdown <minutes>").color(ChatColor.RED).create()));
			}

			int minutes;

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

			for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
				for(ProxiedPlayer pitSimServerPlayer : pitSimServer.getPlayers()) {
					pitSimServerPlayer.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
				}

				pitSimServer.hardShutDown();
			}

			for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
				for(ProxiedPlayer darkzoneServerPlayer : darkzoneServer.getPlayers()) {
					darkzoneServerPlayer.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
				}

				darkzoneServer.hardShutDown();
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

			player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&8&m-----------------------------------------")));

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

			player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&8&m-----------------------------------------")));

		}

		if(args[0].equalsIgnoreCase("suspend")) {

			ServerInfo serverInfo = player.getServer().getInfo();
			if(args.length >= 2) {
				boolean foundServer = false;
				for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
					if(!pitSimServer.getServerInfo().getName().equalsIgnoreCase(args[1])) continue;
					foundServer = true;
					serverInfo = pitSimServer.getServerInfo();
					break;
				}
				if(!foundServer) {
					AOutput.error(player, "&7Could not find a server with that name");
					return;
				}
			}

			boolean kickPlayers = false;
			try {
				kickPlayers = Boolean.parseBoolean(args[2]);
			} catch(Exception ignored) {}

			boolean suspend = false;
			for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
				if(serverInfo == pitSimServer.getServerInfo()) {
					if(pitSimServer.status != ServerStatus.SUSPENDED) {
						suspend = true;
						pitSimServer.status = ServerStatus.SUSPENDED;
						AOutput.color(player, "&aServer has been suspended!");
					} else {
						pitSimServer.status = ServerStatus.RUNNING;
						AOutput.color(player, "&aServer has been un-suspended!");
					}
				}
			}

			boolean darkzone = false;

			for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
				if(serverInfo == darkzoneServer.getServerInfo()) {
					darkzone = true;
					if(darkzoneServer.status != ServerStatus.SUSPENDED) {
						suspend = true;
						darkzoneServer.status = ServerStatus.SUSPENDED;
						AOutput.color(player, "&aServer has been suspended!");
					} else {
						darkzoneServer.status = ServerStatus.RUNNING;
						AOutput.color(player, "&aServer has been un-suspended!");
					}
				}
			}

			if(kickPlayers && suspend) {
				for(ProxiedPlayer proxiedPlayer : serverInfo.getPlayers()) {
					if(proxiedPlayer == player) continue;
					PitSimServerManager.queue(proxiedPlayer, 0, darkzone);
					//TODO: Change to move to other darkzone servers if more are added
				}
			}
		}

		if(args[0].equalsIgnoreCase("edit")) {
			if(args.length < 2) {
				AOutput.error(player, "&cUsage: /admin edit <player>");
				return;
			}

			EditSessionManager.createSession(player.getUniqueId(), args[1]);
		}
	}
}
