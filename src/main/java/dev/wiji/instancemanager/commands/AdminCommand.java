package dev.wiji.instancemanager.commands;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.alogging.ConnectionData;
import dev.wiji.instancemanager.alogging.ConnectionManager;
import dev.wiji.instancemanager.builders.MessageBuilder;
import dev.wiji.instancemanager.discord.DiscordManager;
import dev.wiji.instancemanager.discord.DiscordUser;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.misc.Misc;
import dev.wiji.instancemanager.objects.*;
import dev.wiji.instancemanager.pitsim.DarkzoneServerManager;
import dev.wiji.instancemanager.pitsim.OverworldServerManager;
import dev.wiji.instancemanager.storage.EditSessionManager;
import io.mokulu.discord.oauth.DiscordAPI;
import io.mokulu.discord.oauth.model.User;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
					"&4 * &c/admin gui &7(displays all players and servers in a menu)",
					"&4 * &c/admin suspend [server-name] [kick-players?] &7(maintenance mode)",
					"&4 * &c/admin edit &7(editing playerdata)",
					"&4 * &c/admin connections &7(first connection stats by host)",
					"&4&m--------------------&4<&c&lADMIN&4>&m--------------------"
			).send(player);
			return;
		}

		if(args[0].equalsIgnoreCase("shutdown")) {

			if(args.length < 2) {
				player.sendMessage((new ComponentBuilder("Invalid arguments! /admin shutdown <minutes>").color(ChatColor.RED).create()));
				return;
			}

			int minutes = 0;

			try {
				minutes = Integer.parseInt(args[1]);
			} catch(Exception e) {
				player.sendMessage((new ComponentBuilder("Invalid arguments! /admin shutdown <minutes>").color(ChatColor.RED).create()));
				return;
			}

			if(minutes < 0) {
				player.sendMessage((new ComponentBuilder("Invalid arguments! /admin shutdown <minutes>").color(ChatColor.RED).create()));
				return;
			}

			Server server = player.getServer();
			for(OverworldServer overworldServer : OverworldServerManager.serverList) {
				if(server.getInfo() == overworldServer.getServerInfo()) {
					overworldServer.status = ServerStatus.SHUTTING_DOWN_INITIAL;
					overworldServer.staffOverride = true;
					new PluginMessage().writeString("SHUTDOWN").writeBoolean(false).writeInt(minutes).addServer(overworldServer.getServerInfo()).send();
				}
			}

			for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
				if(server.getInfo() == darkzoneServer.getServerInfo()) {
					darkzoneServer.status = ServerStatus.SHUTTING_DOWN_INITIAL;
					darkzoneServer.staffOverride = true;
					new PluginMessage().writeString("SHUTDOWN").writeBoolean(false).writeInt(minutes).addServer(darkzoneServer.getServerInfo()).send();
				}
			}
		}

		if(args[0].equalsIgnoreCase("stopnetwork")) {

			if(OverworldServerManager.networkIsShuttingDown || DarkzoneServerManager.networkIsShuttingDown) {
				player.sendMessage((new ComponentBuilder("Network is already shutting down!").color(ChatColor.RED).create()));
				return;
			}

			OverworldServerManager.networkIsShuttingDown = true;
			DarkzoneServerManager.networkIsShuttingDown = true;

			player.sendMessage((new ComponentBuilder("Initiated network shutdown!").color(ChatColor.GREEN).create()));
			for(MainGamemodeServer server : MainGamemodeServer.serverList) {
				for(ProxiedPlayer serverPlayer : server.getPlayers()) {
					AOutput.color(serverPlayer, "&c&lTURNING OFF ALL PITSIM SERVERS");
				}
			}

			for(OverworldServer overworldServer : OverworldServerManager.serverList) {
				if(overworldServer.isSuspended()) continue;
				if(overworldServer.status == ServerStatus.RUNNING) {
					overworldServer.shutDown(false);
				}
				if(overworldServer.status == ServerStatus.STARTING) {
					overworldServer.hardShutDown();
				}
			}

			for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
				if(darkzoneServer.isSuspended()) continue;
				if(darkzoneServer.status == ServerStatus.RUNNING) {
					darkzoneServer.shutDown(false);
				}
				if(darkzoneServer.status == ServerStatus.STARTING) {
					darkzoneServer.hardShutDown();
				}
			}
		}

		if(args[0].equalsIgnoreCase("killnetwork")) {

			OverworldServerManager.networkIsShuttingDown = true;
			DarkzoneServerManager.networkIsShuttingDown = true;

			for(OverworldServer overworldServer : OverworldServerManager.serverList) {
				if(overworldServer.isSuspended()) continue;
				for(ProxiedPlayer pitSimServerPlayer : overworldServer.getPlayers()) {
					pitSimServerPlayer.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
				}

				overworldServer.hardShutDown();
			}

			for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
				if(darkzoneServer.isSuspended()) continue;
				for(ProxiedPlayer darkzoneServerPlayer : darkzoneServer.getPlayers()) {
					darkzoneServerPlayer.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
				}

				darkzoneServer.hardShutDown();
			}

			player.sendMessage((new ComponentBuilder("Initiated immediate network shutdown!").color(ChatColor.GREEN).create()));
		}

		if(args[0].equalsIgnoreCase("startnetwork")) {

			if(!OverworldServerManager.networkIsShuttingDown || !DarkzoneServerManager.networkIsShuttingDown) {
				player.sendMessage((new ComponentBuilder("The network isn't shut down!").color(ChatColor.RED).create()));
				return;
			}

			OverworldServerManager.networkIsShuttingDown = false;
			DarkzoneServerManager.networkIsShuttingDown = false;

			player.sendMessage((new ComponentBuilder("Resumed network processes!").color(ChatColor.GREEN).create()));
		}

		if(args[0].equalsIgnoreCase("status")) {

			player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&8&m-----------------------------------------")));

			for(OverworldServer overworldServer : OverworldServerManager.serverList) {
				BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
						"&7[" + overworldServer.getServerInfo().getName() + "] &e(" + overworldServer.getPlayers().size() + ") " +
								overworldServer.status.color + overworldServer.status));

				player.sendMessage(components);
			}

			for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
				BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
						"&7[" + darkzoneServer.getServerInfo().getName() + "] &e(" + darkzoneServer.getPlayers().size() + ") " +
								darkzoneServer.status.color + darkzoneServer.status));

				player.sendMessage(components);
			}

			player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&8&m-----------------------------------------")));

		}

		if(args[0].equalsIgnoreCase("suspend")) {

			ServerInfo serverInfo = player.getServer().getInfo();
			if(args.length >= 2) {
				boolean foundServer = false;
				for(MainGamemodeServer mainGamemodeServer : MainGamemodeServer.serverList) {
					if(!mainGamemodeServer.getServerInfo().getName().equalsIgnoreCase(args[1])) continue;
					foundServer = true;
					serverInfo = mainGamemodeServer.getServerInfo();
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
			for(OverworldServer overworldServer : OverworldServerManager.serverList) {
				if(serverInfo == overworldServer.getServerInfo()) {
					if(overworldServer.status != ServerStatus.SUSPENDED) {
						suspend = true;
						overworldServer.suspendedStatus = overworldServer.status;
						overworldServer.status = ServerStatus.SUSPENDED;
						AOutput.color(player, "&aServer has been suspended!");
					} else {
						if(overworldServer.suspendedStatus != null) {
							if(overworldServer.suspendedStatus == ServerStatus.RESTARTING_FINAL)
								overworldServer.status = ServerStatus.OFFLINE;
							else if(overworldServer.suspendedStatus == ServerStatus.SHUTTING_DOWN_FINAL)
								overworldServer.status = ServerStatus.OFFLINE;
							else overworldServer.status = overworldServer.suspendedStatus;
							overworldServer.suspendedStatus = null;
						} else {
							overworldServer.status = ServerStatus.RUNNING;
						}
						AOutput.color(player, "&aServer has been un-suspended: " + overworldServer.status.color + overworldServer.status);
					}
				}
			}

			boolean darkzone = false;

			for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
				if(serverInfo == darkzoneServer.getServerInfo()) {
					darkzone = true;
					if(darkzoneServer.status != ServerStatus.SUSPENDED) {
						suspend = true;
						darkzoneServer.suspendedStatus = darkzoneServer.status;
						darkzoneServer.status = ServerStatus.SUSPENDED;
						AOutput.color(player, "&aServer has been suspended!");
					} else {
						if(darkzoneServer.suspendedStatus != null) {
							if(darkzoneServer.suspendedStatus == ServerStatus.RESTARTING_FINAL)
								darkzoneServer.status = ServerStatus.OFFLINE;
							else if(darkzoneServer.suspendedStatus == ServerStatus.SHUTTING_DOWN_FINAL)
								darkzoneServer.status = ServerStatus.OFFLINE;
							else darkzoneServer.status = darkzoneServer.suspendedStatus;
							darkzoneServer.suspendedStatus = null;
						} else {
							darkzoneServer.status = ServerStatus.RUNNING;
						}
						AOutput.color(player, "&aServer has been un-suspended: " + darkzoneServer.status.color + darkzoneServer.status);
					}
				}
			}

			if(kickPlayers && suspend) {
				for(ProxiedPlayer proxiedPlayer : serverInfo.getPlayers()) {
					if(proxiedPlayer == player) continue;
					OverworldServerManager.queueFallback(proxiedPlayer, 0, darkzone);
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

		if(args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("menu")) {
			ServerInfo serverInfo = player.getServer().getInfo();
			if(!serverInfo.getName().contains("pitsim") && !serverInfo.getName().contains("darkzone")) {
				AOutput.error(player, "&cYou must be in a PitSim server to use this!");
				return;
			}

			PluginMessage message = new PluginMessage().writeString("ADMIN GUI OPEN").writeString(player.getUniqueId().toString());
			message.addServer(serverInfo).send();
		}

		if(args[0].equalsIgnoreCase("connections") || args[0].equalsIgnoreCase("connect")) {
			if(!player.hasPermission("pitsim.connections")) {
				AOutput.error(player, "&cYou do not have permission to use this command!");
				return;
			}

			if(args.length < 2) {
				ConnectionManager.calculateTotalJoins();
				AOutput.color(player, "&2&m---------------&2<&a&lPLAYER CONNECTIONS&2>&m---------------");
				for(Map.Entry<String, Integer> entry : ConnectionManager.joinMap.entrySet())
					AOutput.color(player, "&2 * &7" + entry.getKey() + ": &a" + entry.getValue());
				AOutput.color(player, "&2&m---------------&2<&a&lPLAYER CONNECTIONS&2>&m---------------");
				return;
			}
			ConnectionData.PlayerConnectionData connectionData = null;
			for(Map.Entry<String, ConnectionData.PlayerConnectionData> entry : ConnectionManager.connectionData.playerConnectionMap.entrySet()) {
				if(!entry.getValue().name.equalsIgnoreCase(args[1]) && !entry.getKey().equalsIgnoreCase(args[1])) continue;
				connectionData = entry.getValue();
				break;
			}
			if(connectionData == null) {
				AOutput.error(player, "&c&lERROR!&7 Could not find a player with that name");
				return;
			}
			AOutput.color(player, "&a&lCONNECT!&7 " + connectionData.name + " &7first connected with: &a" + connectionData.host);
		}

		if(args[0].equalsIgnoreCase("discord")) {
			if(!Misc.isKyro(player.getUniqueId())) {
				AOutput.error(player, "&c&lERROR!&7 You have to be kyro to do this");
				return;
			}

			if(args.length < 2) {
				AOutput.error(player, "&c&lERROR!&7 Usage: /admin discord <forcejoin>");
				return;
			}

			if(args[1].equalsIgnoreCase("forcejoin")) {
				List<UUID> queue = DiscordManager.getAllDiscordUserUUIDs();
				new Thread(() -> {
					while(!queue.isEmpty()) {
						DiscordUser discordUser = DiscordManager.getUser(queue.remove(0));
						try {
							DiscordAPI api = new DiscordAPI(discordUser.accessToken);
							User user = api.fetchUser();

							AOutput.log("Attempting to join account to discord: " + user.getFullUsername());
							discordUser.joinDiscord();
						} catch(Exception exception) {
							continue;
						}
						try {
							Thread.sleep(10_000);
						} catch(InterruptedException exception) {
							throw new RuntimeException(exception);
						}
					}
				}).start();
			}
		}
	}
}
