package net.pitsim.bungee.pitsim;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.ConfigManager;
import net.pitsim.bungee.ProxyRunnable;
import net.pitsim.bungee.ServerManager;
import net.pitsim.bungee.objects.PitSimServer;
import net.pitsim.bungee.objects.ServerStatus;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RestartManager {
	public static boolean proxyRestarting = false;
	public static final long RESTART_TIME = 1000 * 60 * 60 * 12;
	public static final long RESTART_BUFFER = 1000 * 60 * 30;

	public static final long PROXY_RESTART_TIME = 1000 * 60 * 60 * 24 * 7;
	public static final long BACKUP_THRESHOLD = 1000 * 60 * 60 * 24;

	public static long lastBackup = 0;

	static {
		((ProxyRunnable) () -> {

			if(BungeeMain.STARTUP_TIME + PROXY_RESTART_TIME < System.currentTimeMillis()) {
				restartProxy();
			}

			for(PitSimServer activeServer : PitSimServerManager.mixedServerList) {
				if(activeServer.status != ServerStatus.RUNNING) continue;

				if(activeServer.getStartTime() + RESTART_TIME < System.currentTimeMillis()) {

					for(PitSimServer server : PitSimServerManager.mixedServerList) {
						if(activeServer == server || server.status != ServerStatus.RUNNING) continue;
						if((server.getStartTime() + RESTART_TIME) < RESTART_BUFFER + System.currentTimeMillis()) {
							server.setStartTime(server.getStartTime() + RESTART_BUFFER);
						}
					}

					activeServer.shutDown(true);
				}
			}

			if(lastBackup + BACKUP_THRESHOLD < System.currentTimeMillis()) {
				if(ConfigManager.isDev()) return;

				lastBackup = System.currentTimeMillis();
				try {
					FirestoreManager.takeBackup(false);
					FirestoreManager.takeItemBackup();
				} catch(IOException e) { throw new RuntimeException(e); }
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

				for(ProxiedPlayer player : BungeeMain.INSTANCE.getProxy().getPlayers()) {
					player.disconnect(TextComponent.fromLegacyText(ChatColor.RED + "Proxy Restarting"));
				}

				((ProxyRunnable) () -> ServerManager.restartServer(ConfigManager.getProxyServer())).runAfter(5, TimeUnit.SECONDS);

			}

			BungeeMain.INSTANCE.getProxy().broadcast(new ComponentBuilder("Proxy restarting in " + minutes + " minutes!").color(ChatColor.RED).create());
			minutes.getAndDecrement();
		}).runAfterEvery(0, 1, TimeUnit.MINUTES);
	}
}
