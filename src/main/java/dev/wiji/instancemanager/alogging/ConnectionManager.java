package dev.wiji.instancemanager.alogging;

import com.google.gson.Gson;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.pitsim.IdentificationManager;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.*;

public class ConnectionManager implements Listener {
	public static final File dataFile;
	public static final List<String> trackedSubdomains = new ArrayList<>();
	public static ConnectionData connectionData;
	public static Map<String, Integer> joinMap = new LinkedHashMap<>();

	static {
		try {
			dataFile = new File(BungeeMain.INSTANCE.getDataFolder() + "/connection-data.json");
			if(!dataFile.exists()) dataFile.createNewFile();
		} catch(IOException exception) {
			throw new RuntimeException(exception);
		}

		trackedSubdomains.add("mc.pitsim.net");
		trackedSubdomains.add("play.pitsim.net");
		trackedSubdomains.add("vote.pitsim.net");
		trackedSubdomains.add("tomcat.pitsim.net");
		trackedSubdomains.add("bomp.pitsim.net");
		trackedSubdomains.add("pitfall.pitsim.net");
		trackedSubdomains.add("panda.pitsim.net");
		trackedSubdomains.add("planet.pitsim.net");

		connectionData = new ConnectionData();
	}

	@EventHandler
	public void onJoin(LoginEvent event) {
		UUID playerUUID = event.getConnection().getUniqueId();
		String playerName = event.getConnection().getName();
		String hostName = event.getConnection().getVirtualHost().getHostName();
		if(!trackedSubdomains.contains(hostName.toLowerCase())) {
			System.out.println("Player " + playerName + " joined from an untracked host: " + hostName);
			hostName = "mc.pitsim.net";
		}

		IdentificationManager.onLogin(playerUUID, playerName, hostName.toLowerCase());
	}

	public static void calculateTotalJoins() {
		Map<String, Integer> joinMap = new LinkedHashMap<>();
		for(String trackedSubdomain : trackedSubdomains) joinMap.put(trackedSubdomain, 0);
		for(Map.Entry<String, ConnectionData.PlayerConnectionData> entry : connectionData.playerConnectionMap.entrySet()) {
			if(!trackedSubdomains.contains(entry.getValue().host)) continue;
			joinMap.put(entry.getValue().host, joinMap.get(entry.getValue().host) + 1);
		}
		ConnectionManager.joinMap = joinMap;
	}
}
