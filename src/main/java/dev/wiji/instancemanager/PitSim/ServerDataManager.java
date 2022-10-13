package dev.wiji.instancemanager.PitSim;

import dev.wiji.instancemanager.Events.MessageEvent;
import dev.wiji.instancemanager.Objects.*;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServerDataManager implements Listener {

	static {
		((ProxyRunnable) ServerDataManager::sendServerData).runAfterEvery(5, 5, TimeUnit.SECONDS);
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = event.getMessage().getStrings();

		if(strings.size() >= 2 && strings.get(0).equals("SERVER DATA")) {

			String serverName = strings.get(1);
			for(PitSimServer server : PitSimServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					strings.remove(0);
					strings.remove(0);
					server.serverData = new ServerData(strings);
				}
			}

			for(DarkzoneServer server : DarkzoneServerManager.serverList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					strings.remove(0);
					strings.remove(0);
					server.serverData = new ServerData(strings);
				}
			}
		}
	}

	public static void sendServerData() {
		for(PitSimServer pitSimServer : PitSimServerManager.serverList) {
			if(!pitSimServer.status.isOnline()) continue;

			PluginMessage message = new PluginMessage();
			message.writeString("SERVER DATA");

			for(PitSimServer activeServer : PitSimServerManager.serverList) {
				message.writeInt(activeServer.serverData == null ? 0 : activeServer.serverData.getPlayerStrings().size());
				message.writeBoolean(activeServer.status == ServerStatus.RUNNING);

				if(activeServer.serverData != null) {
					for(String playerString : activeServer.serverData.getPlayerStrings()) {
						message.writeString(playerString);
					}
				}

			}

			message.addServer(pitSimServer.getServerInfo().getName()).send();
		}


		for(DarkzoneServer darkzoneServer : DarkzoneServerManager.serverList) {
			if(!darkzoneServer.status.isOnline()) continue;

			PluginMessage message = new PluginMessage();
			message.writeString("SERVER DATA");

			for(DarkzoneServer activeServer : DarkzoneServerManager.serverList) {
				message.writeInt(activeServer.serverData == null ? 0 : activeServer.serverData.getPlayerStrings().size());
				message.writeBoolean(activeServer.status == ServerStatus.RUNNING);

				if(activeServer.serverData != null) {
					for(String playerString : activeServer.serverData.getPlayerStrings()) {
						message.writeString(playerString);
					}
				}

			}

			message.addServer(darkzoneServer.getServerInfo().getName()).send();
		}
	}
}
