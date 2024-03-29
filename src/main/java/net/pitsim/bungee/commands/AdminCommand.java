package net.pitsim.bungee.commands;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.ConfigManager;
import net.pitsim.bungee.alogging.ConnectionData;
import net.pitsim.bungee.alogging.ConnectionManager;
import net.pitsim.bungee.builders.MessageBuilder;
import net.pitsim.bungee.discord.DiscordManager;
import net.pitsim.bungee.discord.DiscordUser;
import net.pitsim.bungee.misc.AOutput;
import net.pitsim.bungee.misc.Misc;
import net.pitsim.bungee.objects.PitSimServer;
import net.pitsim.bungee.objects.PluginMessage;
import net.pitsim.bungee.objects.ServerStatus;
import net.pitsim.bungee.objects.ServerType;
import net.pitsim.bungee.pitsim.PitSimServerManager;
import net.pitsim.bungee.storage.EditSession;
import net.pitsim.bungee.storage.EditSessionManager;
import io.mokulu.discord.oauth.DiscordAPI;
import io.mokulu.discord.oauth.model.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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
					"&4 * &c/admin edit &7(editing playerdata) (-r to reset)",
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
			for(PitSimServer overworldServer : PitSimServerManager.mixedServerList) {
				if(server.getInfo() == overworldServer.getServerInfo()) {
					overworldServer.status = ServerStatus.SHUTTING_DOWN_INITIAL;
					overworldServer.staffOverride = true;
					new PluginMessage().writeString("SHUTDOWN").writeBoolean(false).writeInt(minutes).addServer(overworldServer.getServerInfo()).send();
				}
			}
		}

		if(args[0].equalsIgnoreCase("stopnetwork")) {

			int minutes = -1;
			if(args.length > 1) {
				try {
					minutes = Integer.parseInt(args[1]);
				} catch(Exception e) {
					player.sendMessage((new ComponentBuilder("Invalid arguments! /admin stopnetwork [minutes]").color(ChatColor.RED).create()));
					return;
				}

				if(minutes < 0) {
					player.sendMessage((new ComponentBuilder("Invalid arguments! /admin stopnetwork [minutes]").color(ChatColor.RED).create()));
					return;
				}
			}

			if(PitSimServerManager.networkIsShuttingDown) {
				player.sendMessage((new ComponentBuilder("Network is already shutting down!").color(ChatColor.RED).create()));
				return;
			}

			PitSimServerManager.networkIsShuttingDown = true;

			player.sendMessage((new ComponentBuilder("Initiated network shutdown!").color(ChatColor.GREEN).create()));
			for(PitSimServer server : PitSimServerManager.mixedServerList) {
				for(ProxiedPlayer serverPlayer : server.getPlayers()) {
					AOutput.color(serverPlayer, "&c&lTURNING OFF ALL PITSIM SERVERS");
				}
			}

			for(PitSimServer overworldServer : PitSimServerManager.mixedServerList) {
				if(overworldServer.isSuspended()) continue;
				if(overworldServer.status == ServerStatus.RUNNING) {
					overworldServer.shutDown(false, minutes);
				}
				if(overworldServer.status == ServerStatus.STARTING) {
					overworldServer.hardShutDown();
				}
			}
		}

		if(args[0].equalsIgnoreCase("killnetwork")) {

			PitSimServerManager.networkIsShuttingDown = true;

			for(PitSimServer overworldServer : PitSimServerManager.mixedServerList) {
				if(overworldServer.isSuspended()) continue;

				for(ProxiedPlayer pitSimServerPlayer : overworldServer.getPlayers()) {
					pitSimServerPlayer.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
				}

				overworldServer.hardShutDown();
			}

			player.sendMessage((new ComponentBuilder("Initiated immediate network shutdown!").color(ChatColor.GREEN).create()));
		}

		if(args[0].equalsIgnoreCase("startnetwork")) {

			if(!PitSimServerManager.networkIsShuttingDown) {
				player.sendMessage((new ComponentBuilder("The network isn't shut down!").color(ChatColor.RED).create()));
				return;
			}

			PitSimServerManager.networkIsShuttingDown = false;

			player.sendMessage((new ComponentBuilder("Resumed network processes!").color(ChatColor.GREEN).create()));
		}

		if(args[0].equalsIgnoreCase("status")) {

			player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&8&m-----------------------------------------")));

			for(PitSimServer overworldServer : PitSimServerManager.mixedServerList) {
				BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
						"&7[" + overworldServer.getServerInfo().getName() + "] &e(" + overworldServer.getPlayers().size() + ") " +
								overworldServer.status.color + overworldServer.status));

				player.sendMessage(components);
			}

			player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&8&m-----------------------------------------")));

		}

		if(args[0].equalsIgnoreCase("suspend")) {

			ServerInfo serverInfo = player.getServer().getInfo();
			if(args.length >= 2) {
				boolean foundServer = false;
				for(PitSimServer pitSimServer : PitSimServerManager.mixedServerList) {
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

			ServerType serverType = null;

			boolean suspend = false;
			for(PitSimServer pitSimServer : PitSimServerManager.mixedServerList) {
				if(serverInfo == pitSimServer.getServerInfo()) {
					serverType = pitSimServer.serverType;
					if(pitSimServer.status != ServerStatus.SUSPENDED) {
						suspend = true;
						pitSimServer.suspendedStatus = pitSimServer.status;
						pitSimServer.status = ServerStatus.SUSPENDED;
						AOutput.color(player, "&aServer has been suspended!");
					} else {
						if(pitSimServer.suspendedStatus != null) {
							if(pitSimServer.suspendedStatus == ServerStatus.RESTARTING_FINAL)
								pitSimServer.status = ServerStatus.OFFLINE;
							else if(pitSimServer.suspendedStatus == ServerStatus.SHUTTING_DOWN_FINAL)
								pitSimServer.status = ServerStatus.OFFLINE;
							else pitSimServer.status = pitSimServer.suspendedStatus;
							pitSimServer.suspendedStatus = null;
						} else {
							pitSimServer.status = ServerStatus.RUNNING;
						}
						AOutput.color(player, "&aServer has been un-suspended: " + pitSimServer.status.color + pitSimServer.status);
					}
				}
			}


			if(kickPlayers && suspend) {
				for(ProxiedPlayer proxiedPlayer : serverInfo.getPlayers()) {
					if(proxiedPlayer == player || serverType == null) continue;

					PitSimServerManager manager = PitSimServerManager.getManager(serverType);
					assert manager != null;
					manager.queueFallback(proxiedPlayer, 0, serverType == ServerType.DARKZONE);
				}
			}
		}

		if(args[0].equalsIgnoreCase("edit")) {
			if(args.length < 2) {
				AOutput.error(player, "&cUsage: /admin edit <player>");
				return;
			}

			for(String arg : args) {
				if(arg.equalsIgnoreCase("-r")) {
					String name = args[1];
					ProxiedPlayer target = ProxyServer.getInstance().getPlayer(name);
					UUID uuid = target.getUniqueId();

					EditSession session = EditSessionManager.getSession(uuid);
					if(session == null) {
						AOutput.error(player, "&cThat player does not have an edit session!");
						return;
					}

					session.endSession();
				}
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
				AOutput.error(player, "&c&lERROR!&7 You have to be &9Kyro &7to do this");
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
