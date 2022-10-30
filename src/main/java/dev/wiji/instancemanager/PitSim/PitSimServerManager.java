package dev.wiji.instancemanager.PitSim;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.Guilds.GuildMessaging;
import dev.wiji.instancemanager.Objects.PitSimServer;
import dev.wiji.instancemanager.Objects.PluginMessage;
import dev.wiji.instancemanager.Objects.ServerStatus;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.ServerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PitSimServerManager {
	public static List<PitSimServer> serverList = new ArrayList<>();

	public static final int START_THRESHOLD = 10;
	public static final int STOP_THRESHOLD = 6;

	public static boolean networkIsShuttingDown = false;

	static {
		((ProxyRunnable) () -> {
			int players = getTotalPlayers();

			if(networkIsShuttingDown) return;

			for(PitSimServer pitSimServer : serverList) {
				if(pitSimServer.status == ServerStatus.STARTING) return;
			}

			for(int i = 0; i < Math.min(players / 10 + 1, serverList.size()); i++) {
				PitSimServer server = serverList.get(i);
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

			for(int i = 1 + (players + (START_THRESHOLD - STOP_THRESHOLD - 1)) / 10; i < serverList.size(); i++) {
				PitSimServer server = serverList.get(i);
				if(server.status.isShuttingDown() || server.status == ServerStatus.OFFLINE) continue;
				if(server.status == ServerStatus.RESTARTING_INITIAL) {
					server.status = ServerStatus.SHUTTING_DOWN_INITIAL;
					System.out.println("Switching restart to shut down: " + (i + 1));
					continue;
				}

				System.out.println("Shutting down server: " + (i + 1));
				server.shutDown(false);
			}

		}).runAfterEvery(10, 10, TimeUnit.SECONDS);
	}

	public static void init() {
		for(String value : ServerManager.pitSimServers.values()) serverList.add(new PitSimServer(value));

		for(PitSimServer server : serverList) {
			if(serverList.get(0) == server) {
				server.status = ServerStatus.STARTING;
				ServerManager.restartServer(server.getPteroID());
				continue;
			}

			server.hardShutDown();
		}
	}

	public static boolean queue(ProxiedPlayer player, int requestedServer, boolean fromDarkzone) {

		LeaderboardCalc.sendLeaderboardPlayerData(player.getUniqueId());
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

		if(getTotalServers() == 0) {
			player.sendMessage(new ComponentBuilder("There are currently no available servers. Please try again later.").color(ChatColor.RED).create());
			return false;
		}

		if(ServerChangeListener.recentlyLeft.contains(player)) {
			player.sendMessage(new ComponentBuilder("You recently left a server. Please wait a few seconds before rejoining.").color(ChatColor.RED).create());
			return false;
		}

		ServerDataManager.sendServerData();

		PitSimServer targetServer = null;

		if(requestedServer != 0) {
			targetServer = serverList.get(requestedServer - 1);
			if(targetServer.status != ServerStatus.RUNNING) {
				player.sendMessage(new ComponentBuilder("This server is currently unavailable!").color(ChatColor.RED).create());
				return false;
			}
		}

		int players = getTotalPlayers();

		if(targetServer == null) {
			for(PitSimServer activeServer : serverList) {
				if(activeServer.status != ServerStatus.RUNNING) continue;

				if(activeServer.getPlayers().size() > players / getTotalServers()) continue;
				else {
					targetServer = activeServer;
					break;
				}
			}
		}

		if(targetServer == null) {
			for(PitSimServer pitSimServer : serverList) {
				if(pitSimServer.status == ServerStatus.RUNNING) {
					targetServer = pitSimServer;
					break;
				}
			}
		}

		if(targetServer == null) {

			if(previousServer != null) {
				if(previousServer.status == ServerStatus.RESTARTING_FINAL || previousServer.status == ServerStatus.SHUTTING_DOWN_FINAL) {
					player.connect(BungeeMain.INSTANCE.getProxy().getServerInfo(ConfigManager.getLobbyServer()));
					return true;
				}
			}

			player.sendMessage(new ComponentBuilder("There are currently no available servers. Please try again later.").color(ChatColor.RED).create());
			return false;
		}


		player.sendMessage((new ComponentBuilder("Sending you to " + targetServer.getServerInfo().getName()).color(ChatColor.GREEN).create()));

		if(fromDarkzone) new PluginMessage().writeString("DARKZONE JOIN").writeString(player.getUniqueId().toString()).writeBoolean(true).addServer(targetServer.getServerInfo().getName()).send();
		player.connect(targetServer.getServerInfo());
		return true;
	}

	public static int getTotalPlayers() {
		int total = 0;
		for(PitSimServer server : serverList) {
			total += server.getPlayers().size();
		}
		return total;
	}

	public static int getTotalServers() {
		int total = 0;
		for(PitSimServer server : serverList) {
			if(server.status == ServerStatus.RUNNING || server.status == ServerStatus.RESTARTING_INITIAL) total++;
		}
		return total;
	}

}
