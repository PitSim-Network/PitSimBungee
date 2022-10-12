package dev.wiji.instancemanager.PitSim;

import com.mattmalec.pterodactyl4j.UtilizationState;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.Objects.PitSimServer;
import dev.wiji.instancemanager.Objects.PluginMessage;
import dev.wiji.instancemanager.Objects.ServerData;
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

	static {
		((ProxyRunnable) () -> {
			int players = getTotalPlayers();

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

				server.status = ServerStatus.RUNNING;
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

		}).runAfterEvery(2, 2, TimeUnit.MINUTES);
	}

	public static void init() {

		int i = 1;
		for(String value : ServerManager.pitSimServers.values()) {
			serverList.add(new PitSimServer(value, i));
			i++;
		}

		PitSimServer mainServer = serverList.get(0);

		List<PitSimServer> recoveredServers = new ArrayList<>();

		for(PitSimServer pitSimServer : serverList) {
			UtilizationState state = pitSimServer.getState();
			if(state == UtilizationState.RUNNING) {
				if(ConfigManager.configuration.getLong(pitSimServer.getPteroID()) == 0) {
					pitSimServer.hardShutDown();
					continue;
				}
				System.out.println("Recovered " + pitSimServer.getServerIndex());
				pitSimServer.setStartTime(ConfigManager.configuration.getLong(pitSimServer.getPteroID()));
				ConfigManager.configuration.set(pitSimServer.getPteroID(), null);
				activeServers.add(pitSimServer);
				recoveredServers.add(pitSimServer);
			}
			else if(state == UtilizationState.STARTING) {
				if(ConfigManager.configuration.getLong(pitSimServer.getPteroID()) == 0) {
					pitSimServer.hardShutDown();
					continue;
				}
				pitSimServer.startUp(true);
				recoveredServers.add(pitSimServer);
			}
		}

		if(!recoveredServers.contains(mainServer)) mainServer.startUp(false);

		int highestServer = 1;
		for(PitSimServer recoveredServer : recoveredServers) {
			if(recoveredServer.getServerIndex() > highestServer) {
				highestServer = recoveredServer.getServerIndex();
			}
		}

		for(PitSimServer pitSimServer : serverList) {
			if(pitSimServer.getServerIndex() < highestServer && !recoveredServers.contains(pitSimServer)) {
				pitSimServer.startUp(false);
			}
		}

	}

	public static boolean queue(ProxiedPlayer player, int requestedServer) {
		if(getTotalServers() == 0) return false;

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
			player.sendMessage(new ComponentBuilder("There are currently no available servers. Please try again later.").color(ChatColor.RED).create());
			return false;
		}


		player.sendMessage((new ComponentBuilder("Sending you to " + targetServer.getServerInfo().getName()).color(ChatColor.GREEN).create()));
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
