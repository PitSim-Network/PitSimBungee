package dev.wiji.instancemanager.pitsim;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import dev.wiji.instancemanager.*;
import dev.wiji.instancemanager.commands.LobbiesCommand;
import dev.wiji.instancemanager.commands.ServerJoinCommand;
import dev.wiji.instancemanager.discord.AuthenticationManager;
import dev.wiji.instancemanager.guilds.GuildMessaging;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.*;
import dev.wiji.instancemanager.storage.EditSessionManager;
import dev.wiji.instancemanager.storage.StorageManager;
import dev.wiji.instancemanager.storage.StorageProfile;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PitSimServerManager {

	public static List<PitSimServerManager> managers = new ArrayList<>();
	public static boolean networkIsShuttingDown = false;
	public static List<PitSimServer> mixedServerList = new ArrayList<>();

	public ServerType serverType;
	public List<PitSimServer> serverList;

	//	The next server turns on when the player count reaches a multiple of this number
	public final int NEW_SERVER_THRESHOLD;
	//	When the player count drops this many below a multiple of the number above, that server enabled by hitting
	//	that threshold is no longer needed and gets shut down
	public final int REQUIRED_DROP_FOR_SHUTDOWN;

	//Queue info
	public static final int JOINS_PER_SECOND = 5;
	private int currentJoinCount = 0;
	public static List<ProxiedPlayer> queuingPlayers = new ArrayList<>();

	public PitSimServerManager(ServerType serverType, int newServerThreshold, int requiredDropForShutdown) {
		this.serverType = serverType;
		this.serverList = new ArrayList<>();

		this.NEW_SERVER_THRESHOLD = newServerThreshold;
		this.REQUIRED_DROP_FOR_SHUTDOWN = requiredDropForShutdown;

		registerServers();

		((ProxyRunnable) () -> {
			PluginMessage playerMessage = new PluginMessage().writeString("PLAYER COUNT").writeInt(getTotalPlayersUnvanished());
			for(PitSimServer server : PitSimServerManager.mixedServerList) {
				if(!server.status.isOnline()) continue;
				playerMessage.addServer(server.getServerInfo());
			}
			if(!ConfigManager.isDev()) playerMessage.addServer("lobby");
			playerMessage.send();

			int players = getTotalPlayers();

			if(networkIsShuttingDown) return;

			for(PitSimServer pitSimServer : serverList) {
				if(pitSimServer.status == ServerStatus.STARTING) return;
			}

			for(int i = 0; i < Math.min(players / NEW_SERVER_THRESHOLD + 1, serverList.size()); i++) {
				PitSimServer server = serverList.get(i);

				if(server.staffOverride) continue;

				if(server.status.isOnline()) {
					if(server.status == ServerStatus.SHUTTING_DOWN_INITIAL) {
						server.status = ServerStatus.RUNNING;
						new PluginMessage().writeString("CANCEL SHUTDOWN").addServer(server.getServerInfo().getName()).send();
					}
					continue;
				}

				if(server.isOnStartCooldown) continue;

				server.status = ServerStatus.STARTING;
				server.startUp();
				System.out.println("Turning on server: " + (i + 1));
			}

			for(int i = 1 + (players + REQUIRED_DROP_FOR_SHUTDOWN - 1) / NEW_SERVER_THRESHOLD; i < serverList.size(); i++) {
				PitSimServer server = serverList.get(i);
				if(server.status.isShuttingDown() || server.status == ServerStatus.OFFLINE || server.status == ServerStatus.SUSPENDED)
					continue;
				if(server.status == ServerStatus.RESTARTING_INITIAL) {
					server.status = ServerStatus.SHUTTING_DOWN_INITIAL;
					System.out.println("Switching restart to shut down: " + (i + 1));
					continue;
				}

				System.out.println("Shutting down server: " + (i + 1));
				server.shutDown(false);
			}

		}).runAfterEvery(10, 10, TimeUnit.SECONDS);

		managers.add(this);

		((ProxyRunnable) () -> currentJoinCount = 0).runAfterEvery(0, 1, TimeUnit.SECONDS);
	}

	public void registerServers() {
		for(String value : serverType.getServerStrings().values()) {
			Class<? extends PitSimServer> clazz = serverType.getServerClass();

			try {
				PitSimServer server = clazz.getConstructor(String.class).newInstance(value);
				serverList.add(server);
				System.out.println("Registered server: " + value + " (" + serverType.name() + ")");
				mixedServerList.add(server);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		for(PitSimServer server : serverList) {
			if(serverList.get(0) == server) {

				if(ConfigManager.isDev()) {
					server.status = ServerStatus.RUNNING;
					server.setStartTime(System.currentTimeMillis());
					continue;
				}

				server.status = ServerStatus.STARTING;
				ServerManager.restartServer(server.getPteroID());
			}

			server.hardShutDown();
		}
	}

	public boolean queueFallback(ProxiedPlayer player, int requestedServer, boolean fromDarkzone) {
		boolean success = queue(player, requestedServer, fromDarkzone);
		if(!success) {
			player.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
		}
		return success;
	}

	public boolean queue(ProxiedPlayer player, int requestedServer, boolean fromDarkzone) {

		if(ServerChangeListener.recentlyLeft.contains(player.getUniqueId())) {
			player.sendMessage(new ComponentBuilder("You recently left a server. Please wait a few seconds before rejoining.").color(ChatColor.RED).create());
			return false;
		}

		if(!LockdownManager.canJoin(player)) {
			return false;
		}


		if(queuingPlayers.contains(player)) {
			AOutput.color(player, "&eYou are already in the queue!");
			return false;
		}

		if(currentJoinCount > JOINS_PER_SECOND || CommandBlocker.blockedPlayers.contains(player.getUniqueId()) ||
				ServerChangeListener.recentlyLeft.contains(player.getUniqueId())) {

			if(queuingPlayers.contains(player)) return false;

			AOutput.color(player, "&eQueuing you to find a server!");
			queuingPlayers.add(player);
			((ProxyRunnable) () -> {
				queuingPlayers.remove(player);
				queue(player, requestedServer, fromDarkzone);
			}).runAfter(3, TimeUnit.SECONDS);
			return true;
		}

		currentJoinCount++;


//		//TODO: Make sure that commands run between SWITCH request is sent and received do not cause issues
//		if(CommandBlocker.blockedPlayers.contains(player.getUniqueId())) {
//			player.sendMessage((new ComponentBuilder("Please wait a moment before Queuing again").color(ChatColor.RED).create()));
//			return false;
//		}

		CommandBlocker.blockPlayer(player.getUniqueId());

		if(EditSessionManager.isBeingEdited(player.getUniqueId())) {
			player.sendMessage(new ComponentBuilder("Your player-data is being modified. Please try again in a moment.").color(ChatColor.RED).create());
			return false;
		}

		if(!ConfigManager.isDev()) {
			try {
				LeaderboardCalc.sendLeaderboardPlayerData(player.getUniqueId());
			} catch(Exception e) {
				System.out.println("Player leaderboard data send failed. (Proxy has just started)");
				return false;
			}
		}

		GuildMessaging.sendGuildData(player);

		PitSimServer previousServer = null;
		for(PitSimServer server : serverList) {
			if(server.getServerInfo() == player.getServer().getInfo()) {
				previousServer = server;
				break;
			}
		}

		if(previousServer != null) {
			if(previousServer.status == ServerStatus.RESTARTING_FINAL || previousServer.status == ServerStatus.SHUTTING_DOWN_FINAL) {
				if(networkIsShuttingDown) {
					player.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
				}
			}
		}

		ServerDataManager.sendServerData();

		PitSimServer targetServer = null;

		if(requestedServer != 0) {
			targetServer = serverList.get(requestedServer - 1);

			if(player.hasPermission("pitsim.join") || ServerJoinCommand.permissionBypass.contains(player.getUniqueId())) {
				if(!targetServer.status.isOnline()) {
					player.sendMessage(new ComponentBuilder("This server is currently unavailable!").color(ChatColor.RED).create());
					return false;
				}
			} else if(targetServer.status != ServerStatus.RUNNING) {
				player.sendMessage(new ComponentBuilder("This server is currently unavailable!").color(ChatColor.RED).create());
				return false;
			}

			ServerJoinCommand.permissionBypass.remove(player.getUniqueId());
		}

		if(getTotalServersOnline() == 0 && targetServer == null) {
			player.sendMessage(new ComponentBuilder("There are currently no available servers. Please try again later.").color(ChatColor.RED).create());
			return false;
		}

		int players = getTotalPlayers();

		if(targetServer == null) {
			ServerInfo current = player.getServer().getInfo();
			for(PitSimServer activeServer : serverList) {
				if(activeServer.status != ServerStatus.RUNNING) continue;
				if(activeServer.getServerInfo() == current) continue;
				if(activeServer.getPlayers().size() > players / getTotalServersOnline()) continue;
				targetServer = activeServer;
				break;
			}
		}

		if(targetServer == null) {
			ServerInfo current = player.getServer().getInfo();

			for(PitSimServer pitSimServer : serverList) {
				if(pitSimServer.status == ServerStatus.RUNNING && current != pitSimServer.getServerInfo()) {
					targetServer = pitSimServer;
					break;
				}
			}
		}

		if(targetServer == null) {

			if(previousServer != null) {
				if(previousServer.status == ServerStatus.RESTARTING_FINAL || previousServer.status == ServerStatus.SHUTTING_DOWN_FINAL || previousServer.status == ServerStatus.SUSPENDED) {
					player.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
					return true;
				}
			}

			player.sendMessage(new ComponentBuilder("There are currently no available servers. Please try again later.").color(ChatColor.RED).create());
			return false;
		}

		StorageProfile profile = StorageManager.getStorage(player.getUniqueId());
		profile.sendToServer(targetServer.getServerInfo());

		player.sendMessage((new ComponentBuilder("Sending you to " + targetServer.getServerInfo().getName()).color(ChatColor.GREEN).create()));

		if(fromDarkzone && serverType != ServerType.DARKZONE)
			new PluginMessage().writeString("DARKZONE JOIN").writeString(player.getUniqueId().toString()).writeBoolean(true).addServer(targetServer.getServerInfo().getName()).send();

		PitSimServer finalTargetServer = targetServer;
		((ProxyRunnable) () -> player.connect(finalTargetServer.getServerInfo())).runAfter(1, TimeUnit.SECONDS);

		if(AuthenticationManager.rewardVerificationList.contains(player.getUniqueId())) {
			((ProxyRunnable) () -> {
				if(player.getServer().getInfo() != finalTargetServer.getServerInfo()) return;
				AuthenticationManager.rewardPlayer(player);
				AuthenticationManager.rewardVerificationList.remove(player.getUniqueId());
			}).runAfter(3, TimeUnit.SECONDS);
		}

		return true;
	}

	public int getTotalPlayers() {
		if(LobbiesCommand.overridePlayers) return 50;
		int total = 0;
		for(PitSimServer server : serverList) {
			total += server.getPlayers().size();
		}
		return total;
	}

	public static int getTotalPlayersUnvanished() {
		int total = 0;
		for(PitSimServer server : PitSimServerManager.mixedServerList) {
			for(ProxiedPlayer player : server.getPlayers()) {
				if(!BungeeVanishAPI.isInvisible(player)) total++;
			}
		}
		return total;
	}

	public int getTotalServersOnline() {
		int total = 0;
		for(PitSimServer server : serverList) {
			if(server.status == ServerStatus.RUNNING || server.status == ServerStatus.RESTARTING_INITIAL) total++;
		}

		return total;
	}

	public static PitSimServerManager getManager(ServerType serverType) {
		for(PitSimServerManager manager : managers) {
			if(manager.serverType == serverType) return manager;
		}
		return null;
	}
}
