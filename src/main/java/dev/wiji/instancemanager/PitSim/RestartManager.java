package dev.wiji.instancemanager.PitSim;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.Objects.DarkzoneServer;
import dev.wiji.instancemanager.Objects.PitSimServer;
import dev.wiji.instancemanager.Objects.ServerStatus;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.ServerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RestartManager {
	public static boolean proxyRestarting = false;
	public static long RESTART_TIME = 21600000;
	public static long RESTART_BUFFER = 1800000;

	public static long PROXY_RESTART_TIME = 172800000;

	static {
		((ProxyRunnable) () -> {

			if(BungeeMain.STARTUP_TIME + PROXY_RESTART_TIME < System.currentTimeMillis()) {
				restartProxy();
			}


			for(PitSimServer activeServer : PitSimServerManager.serverList) {
				if(activeServer.status != ServerStatus.RUNNING) continue;

				if(activeServer.getStartTime() + RESTART_TIME < System.currentTimeMillis()) {

					for(PitSimServer server : PitSimServerManager.serverList) {
						if(activeServer == server || server.status != ServerStatus.RUNNING) continue;
						if((server.getStartTime() + RESTART_TIME) < RESTART_BUFFER + System.currentTimeMillis()) {
							server.setStartTime(server.getStartTime() + RESTART_BUFFER);
						}
					}

					activeServer.shutDown(true);
				}
			}


			for(DarkzoneServer activeServer : DarkzoneServerManager.serverList) {
				if(activeServer.status != ServerStatus.RUNNING) continue;

				if(activeServer.getStartTime() + RESTART_TIME < System.currentTimeMillis()) {

					for(DarkzoneServer server : DarkzoneServerManager.serverList) {
						if(activeServer == server || server.status != ServerStatus.RUNNING) continue;
						if((server.getStartTime() + RESTART_TIME) < RESTART_BUFFER + System.currentTimeMillis()) {
							server.setStartTime(server.getStartTime() + RESTART_BUFFER);
						}
					}

					activeServer.shutDown(true);
				}
			}

		}).runAfterEvery(1, 1, TimeUnit.MINUTES);
	}

	public static void init() {
		System.out.println("Restart manager Init!");
	}


	public static void restartProxy() {
		if(proxyRestarting) return;
		proxyRestarting = true;

		AtomicInteger minutes = new AtomicInteger(10);

		((ProxyRunnable) () -> {

			if(minutes.get() == 0) {
				BungeeMain.INSTANCE.getProxy().broadcast(new ComponentBuilder("Proxy restarting!").color(ChatColor.RED).create());

				ServerManager.stopServer(ConfigManager.getProxyServer());
			}

			BungeeMain.INSTANCE.getProxy().broadcast(new ComponentBuilder("Proxy restarting in " + minutes + " minutes!").color(ChatColor.RED).create());
			minutes.getAndDecrement();
		}).runAfterEvery(0, 1, TimeUnit.MINUTES);
	}
}
