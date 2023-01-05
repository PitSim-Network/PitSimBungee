package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.objects.DarkzoneServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.objects.ServerStatus;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.ServerManager;
import dev.wiji.instancemanager.objects.DarkzoneServer;
import dev.wiji.instancemanager.objects.MainGamemodeServer;
import dev.wiji.instancemanager.objects.PluginMessage;
import dev.wiji.instancemanager.objects.ServerStatus;
import dev.wiji.instancemanager.storage.EditSessionManager;
import dev.wiji.instancemanager.storage.StorageManager;
import dev.wiji.instancemanager.storage.StorageProfile;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DarkzoneServerManager {
	public static List<DarkzoneServer> serverList = new ArrayList<>();

	public static final int START_THRESHOLD = 10;
	public static final int STOP_THRESHOLD = 6;

	public static boolean networkIsShuttingDown = false;

	static {
		((ProxyRunnable) () -> {

			if(networkIsShuttingDown) return;

			int players = getTotalPlayers();

			for(DarkzoneServer darkzoneServer : serverList) {
				if(darkzoneServer.status == ServerStatus.STARTING) return;
			}

			for(int i = 0; i < Math.min(players / 10 + 1, serverList.size()); i++) {
				DarkzoneServer server = serverList.get(i);
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
				System.out.println("Turning on darkzone server: " + (i + 1));
			}

			for(int i = 1 + (players + (START_THRESHOLD - STOP_THRESHOLD - 1)) / 10; i < serverList.size(); i++) {
				DarkzoneServer server = serverList.get(i);
				if(server.status.isShuttingDown() || server.status == ServerStatus.OFFLINE || server.status == ServerStatus.SUSPENDED)
					continue;
				if(server.status == ServerStatus.RESTARTING_INITIAL) {
					server.status = ServerStatus.SHUTTING_DOWN_INITIAL;
					System.out.println("Switching restart to shut down: " + (i + 1));
					continue;
				}

				System.out.println("Shutting down darkzone server: " + (i + 1));
				server.shutDown(false);
			}

		}).runAfterEvery(10, 10, TimeUnit.SECONDS);
	}

	public static void init() {
		for(String value : ServerManager.darkzoneServers.values()) serverList.add(new DarkzoneServer(value));

		for(DarkzoneServer server : serverList) {
			if(ConfigManager.isDev()) {
				server.status = ServerStatus.RUNNING;
				server.setStartTime(System.currentTimeMillis());
				continue;
			}

			server.status = ServerStatus.STARTING;
			ServerManager.restartServer(server.getPteroID());
		}
	}

	public static boolean queueFallback(ProxiedPlayer player, int requestedServer) {
		boolean success = queue(player, requestedServer);
		if(!success) {
			player.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
		}
		return success;
	}

	public static boolean queue(ProxiedPlayer player, int requestedServer) {

		if(ServerChangeListener.recentlyLeft.contains(player)) {
			player.sendMessage(new ComponentBuilder("You recently left a server. Please wait a few seconds before rejoining.").color(ChatColor.RED).create());
			return false;
		}

		if(MainGamemodeServer.cooldownPlayers.containsKey(player)) {
			long time = MainGamemodeServer.cooldownPlayers.get(player);

			if(time + CommandListener.COOLDOWN_SECONDS * 1000 < System.currentTimeMillis()) {
				MainGamemodeServer.cooldownPlayers.remove(player);
			} else {
				player.sendMessage((new ComponentBuilder("Please wait a moment before Queuing again").color(ChatColor.RED).create()));
				return false;
			}
		}

		MainGamemodeServer.cooldownPlayers.put(player.getUniqueId(), System.currentTimeMillis());

		if(EditSessionManager.isBeingEdited(player.getUniqueId())) {
			player.sendMessage(new ComponentBuilder("Your player-data is being modified. Please try again in a moment.").color(ChatColor.RED).create());
			return false;
		}

		DarkzoneServer previousServer = null;
		for(DarkzoneServer server : serverList) {
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

		DarkzoneServer targetServer = null;

		if(requestedServer != 0) {
			targetServer = serverList.get(requestedServer - 1);

			if(player.hasPermission("pitsim.join")) {
				if(!targetServer.status.isOnline()) {
					player.sendMessage(new ComponentBuilder("This server is currently unavailable!").color(ChatColor.RED).create());
					return false;
				}
			} else if(targetServer.status != ServerStatus.RUNNING) {
				player.sendMessage(new ComponentBuilder("This server is currently unavailable!").color(ChatColor.RED).create());
				return false;
			}
		}

		if(getTotalServers() == 0 && targetServer == null) {
			player.sendMessage(new ComponentBuilder("There are currently no available servers. Please try again later.").color(ChatColor.RED).create());
			return false;
		}

		int players = getTotalPlayers();

		if(targetServer == null) {
			for(DarkzoneServer activeServer : serverList) {
				if(activeServer.status != ServerStatus.RUNNING) continue;

				if(activeServer.getPlayers().size() > players / getTotalServers()) continue;
				else {
					targetServer = activeServer;
					break;
				}
			}
		}

		if(targetServer == null) {
			for(DarkzoneServer darkzoneServer : serverList) {
				if(darkzoneServer.status == ServerStatus.RUNNING) {
					targetServer = darkzoneServer;
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
//		System.out.println("the profile that we are sending: " + profile);
		profile.sendToServer(targetServer.getServerInfo(), false);

		player.sendMessage((new ComponentBuilder("Sending you to " + targetServer.getServerInfo().getName()).color(ChatColor.GREEN).create()));

		player.connect(targetServer.getServerInfo());

		return true;
	}

	public static int getTotalPlayers() {
		int total = 0;
		for(DarkzoneServer server : serverList) {
			total += server.getPlayers().size();
		}
		return total;
	}

	public static int getTotalServers() {
		int total = 0;
		for(DarkzoneServer server : serverList) {
			if(server.status == ServerStatus.RUNNING || server.status == ServerStatus.RESTARTING_INITIAL) total++;
		}
		return total;
	}

}
