package dev.wiji.instancemanager.alogging;

import com.google.gson.Gson;
import dev.wiji.instancemanager.BungeeMain;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.*;

public class ConnectionManager implements Listener {
	public static final File dataFile = new File(BungeeMain.INSTANCE.getDataFolder() + "/connection-data.json");
	public static final List<String> trackedSubdomains = new ArrayList<>();
	public static ConnectionData connectionData;

	static {
		trackedSubdomains.add("mc.pitsim.net");
		trackedSubdomains.add("play.pitsim.net");
		trackedSubdomains.add("vote.pitsim.net");
		trackedSubdomains.add("tomcat.pitsim.net");
		trackedSubdomains.add("bomp.pitsim.net");
		trackedSubdomains.add("pitfall.pitsim.net");

		try {
			Reader reader = Files.newBufferedReader(dataFile.toPath());
			connectionData = new Gson().fromJson(reader, ConnectionData.class);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@EventHandler
	public void onJoin(LoginEvent event) {
		UUID playerUUID = event.getConnection().getUniqueId();
		String playerName = event.getConnection().getName();
		String hostName = event.getConnection().getVirtualHost().getHostName();
		if(!trackedSubdomains.contains(hostName.toLowerCase())) {
			System.out.println("Player " + playerName + " joined from an untracked host: " + hostName);
			return;
		}

		if(connectionData.playerConnectionMap.containsKey(playerUUID.toString())) return;
		connectionData.playerConnectionMap.put(playerUUID.toString(), new ConnectionData.PlayerConnectionData(playerName, hostName.toLowerCase()));
		connectionData.save();
	}

	public static Map<String, Integer> getTotalJoins() {
		Map<String, Integer> joinMap = new LinkedHashMap<>();
		for(String trackedSubdomain : trackedSubdomains) joinMap.put(trackedSubdomain, 0);
		for(Map.Entry<String, ConnectionData.PlayerConnectionData> entry : connectionData.playerConnectionMap.entrySet())
			joinMap.put(entry.getValue().host, joinMap.get(entry.getValue().host) + 1);
		return joinMap;
	}
}
