package dev.wiji.instancemanager;

import com.mattmalec.pterodactyl4j.UtilizationState;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.Utilization;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerManager {
	public static List<String> inactiveServers = new ArrayList<>();
	public static Map<String, String> pitSimServers = new HashMap<>();
	public static Map<String, String> darkzoneServers = new HashMap<>();

	public static void onEnable() {
		for(String inactiveServer : inactiveServers) {
			killServer(inactiveServer);
		}
	}

	public static void startServer(String identifier) {
		BungeeMain.client.retrieveServerByIdentifier(identifier)
				.flatMap(ClientServer::start).executeAsync();
	}

	public static void stopServer(String identifier) {
		BungeeMain.client.retrieveServerByIdentifier(identifier)
				.flatMap(ClientServer::stop).executeAsync();
	}

	public static void killServer(String identifier) {
		BungeeMain.client.retrieveServerByIdentifier(identifier)
				.flatMap(ClientServer::kill).executeAsync();
	}

	public static void restartServer(String identifier) {
		BungeeMain.client.retrieveServerByIdentifier(identifier)
				.flatMap(ClientServer::restart).executeAsync();
	}

	public static UtilizationState getState(String identifier) {
//		BungeeMain.client.retrieveServerByIdentifier(identifier).flatMap();
		ClientServer server = BungeeMain.client.retrieveServerByIdentifier(identifier).execute();
		Utilization utilization = server.retrieveUtilization().execute();
		return utilization.getState();
	}

	public static void runCommand(String identifier, String command) {
		ClientServer server = BungeeMain.client.retrieveServerByIdentifier(identifier).execute();
		BungeeMain.client.sendCommand(server, command).execute();
	}

}
