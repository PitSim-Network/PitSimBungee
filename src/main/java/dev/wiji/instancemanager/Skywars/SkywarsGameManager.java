package dev.wiji.instancemanager.Skywars;

import com.mattmalec.pterodactyl4j.UtilizationState;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.ServerManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SkywarsGameManager {
	public static String mainQueueServer;
	public static int mainQueuePlayers = 0;
	public static String backupQueueServer;
	public static int backupQueuePlayers = 0;
	public static List<String> startingServers = new ArrayList<>();
	public static Map<String, ScheduledTask> activeServers = new HashMap<>();

	public static void onStart(String id) {
		System.out.println("Enabled! " + id);
		ServerManager.runCommand(id, "gameload skywars");
		startingServers.remove(id);
		if(mainQueueServer == null) mainQueueServer = id;
		else if(backupQueueServer == null) backupQueueServer = id;
		else {
			ServerManager.killServer(mainQueueServer);
			ServerManager.inactiveServers.add(mainQueueServer);
			mainQueueServer = backupQueueServer;
			backupQueueServer = id;
		}
	}

	public static void fetchServer() {
		if(ServerManager.inactiveServers.size() == 0) {
			waitForServer();
			return;
		}
		String initialServer = ServerManager.inactiveServers.get(0);

		ServerManager.inactiveServers.remove(initialServer);
		startingServers.add(initialServer);
		startServer(initialServer);
	}

	public static void startServer(String id) {
		ServerManager.startServer(id);

		new ProxyRunnable() {
			@Override
			public void run() {

				UtilizationState state = ServerManager.getState(id);
				if(state == UtilizationState.RUNNING) {
					onStart(id);
				} else {
					ServerManager.killServer(id);
					ServerManager.inactiveServers.add(id);
					fetchServer();
				}
				startingServers.remove(id);
				System.out.println(mainQueueServer);
			}
		}.runAfter(20, TimeUnit.SECONDS);



	}

	public static ScheduledTask task;

	public static void waitForServer() {

		task = new ProxyRunnable() {
			@Override
			public void run() {
				if(ServerManager.inactiveServers.size() == 0) return;
				else {
					String initialServer = ServerManager.inactiveServers.get(0);
					ServerManager.inactiveServers.remove(initialServer);
					startingServers.add(initialServer);
					startServer(initialServer);
					task.cancel();
				}
			}
		}.runAfterEvery(10,10, TimeUnit.SECONDS);
	}
}
