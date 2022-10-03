package dev.wiji.instancemanager.PitSim;

import com.mattmalec.pterodactyl4j.UtilizationState;
import dev.wiji.instancemanager.Objects.PitSimServer;
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
	public static List<PitSimServer> activeServers = new ArrayList<>();

	static {
		((ProxyRunnable) () -> {
			int players = getTotalPlayers();
			int serversToActivate = (int) (Math.floor(players / 10) + 1) - activeServers.size();

			if(serversToActivate < 0) {
				for(int i = 0; i < Math.abs(serversToActivate); i++) {
					if(activeServers.size() == 1) break;

					PitSimServer server = activeServers.get(activeServers.size() - 1);
					server.shutDown();
					activeServers.remove(server);
				}
				return;
			}

			if(serversToActivate > serverList.size() - activeServers.size()) {
				serversToActivate = serverList.size() - activeServers.size();
			}

			for(int i = 0; i < serversToActivate; i++) {
				serverList.get(activeServers.size() + i).startUp(false);
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
				activeServers.add(pitSimServer);
				recoveredServers.add(pitSimServer);
			}
			else if(state == UtilizationState.STARTING) {
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

	public static boolean queue(ProxiedPlayer player) {

		if(activeServers.size() == 0 || !activeServers.contains(serverList.get(0))) return false;

		PitSimServer targetServer = null;
		int players = getTotalPlayers();

		for(PitSimServer activeServer : activeServers) {
			if(activeServer.getPlayers().size() > players / activeServers.size()) continue;
			else targetServer = activeServer;
		}

		if(targetServer == null) targetServer = activeServers.get(0);
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
}
