package dev.wiji.instancemanager.PitSim;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Events.MessageEvent;
import dev.wiji.instancemanager.Objects.PitSimServer;
import dev.wiji.instancemanager.Objects.PluginMessage;
import dev.wiji.instancemanager.Objects.ServerData;
import dev.wiji.instancemanager.Objects.ServerStatus;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

public class MessageListener implements Listener {

	@EventHandler
	public void onMessage(MessageEvent event) {

		PluginMessage message = event.getMessage();
		List<String> strings = event.getMessage().getStrings();
		List<Integer> integers = event.getMessage().getIntegers();

		if(strings.size() >= 2 && strings.get(0).equals("INITIATE STARTUP")) {
			String serverName = strings.get(1);
			for(PitSimServer server : PitSimServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {

					if(server.status.isShuttingDown()) {
						server.hardShutDown();
					} else {
						System.out.println("Server " + serverName + " is now running!");
						server.status = ServerStatus.RUNNING;
						server.setStartTime(System.currentTimeMillis());
						break;
					}
				}
			}

		}

		if(strings.size() >= 2 && strings.get(0).equals("INITIATE FINAL SHUTDOWN")) {
			String serverName = strings.get(1);
			for(PitSimServer server : PitSimServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {

					server.serverData = null;
					for(ProxiedPlayer player : server.getPlayers()) {
						PitSimServerManager.queue(player, 0);
					}

					server.status = ServerStatus.SHUTTING_DOWN_FINAL;
					server.beginStartCooldown();
				}
			}

		}

		if(strings.size() >= 2 && strings.get(0).equals("INITIATE FINAL RESTART")) {
			String serverName = strings.get(1);
			for(PitSimServer server : PitSimServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {

					server.serverData = null;
					for(ProxiedPlayer player : server.getPlayers()) {
						PitSimServerManager.queue(player, 0);
					}

					server.status = ServerStatus.RESTARTING_FINAL;
					server.beginStartCooldown();
				}
			}

		}

		if(strings.size() >= 3 && strings.get(0).equals("STATUS REPORT")) {
			String serverName = strings.get(1);
			String status = strings.get(2);
			for(PitSimServer server : PitSimServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					server.status = ServerStatus.valueOf(status);
					break;
				}
			}

		}


		if(strings.size() >= 2 && strings.get(0).equals("QUEUE")) {

			String playerString = strings.get(1);
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerString);
			if(player == null) return;

			int requested = 0;
			if(integers.size() >= 1) {
				requested = integers.get(0);
			}

			PitSimServerManager.queue(player, requested);
		}
	}

}
