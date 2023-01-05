package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.objects.*;
import dev.wiji.instancemanager.ProxyRunnable;
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
			for(OverworldServer server : OverworldServerManager.serverList) {
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
		for(MainGamemodeServer overworldServer : MainGamemodeServer.serverList) {
			if(!overworldServer.status.isOnline()) continue;

			PluginMessage message = new PluginMessage();
			message.writeString("SERVER DATA");

			for(OverworldServer activeServer : OverworldServerManager.serverList) {
				message.writeInt(activeServer.serverData == null ? 0 : activeServer.serverData.getPlayerStrings().size());
				message.writeBoolean(activeServer.status == ServerStatus.RUNNING);

				if(activeServer.serverData != null) {
					for(String playerString : activeServer.serverData.getPlayerStrings()) {
						message.writeString(playerString);
					}
				}

			}

			message.addServer(overworldServer.getServerInfo().getName()).send();

			PluginMessage dzMessage = new PluginMessage();
			dzMessage.writeString("DARKZONE SERVER DATA");

			for(DarkzoneServer activeServer : DarkzoneServerManager.serverList) {
				dzMessage.writeInt(activeServer.serverData == null ? 0 : activeServer.serverData.getPlayerStrings().size());
				dzMessage.writeBoolean(activeServer.status == ServerStatus.RUNNING);

				if(activeServer.serverData != null) {
					for(String playerString : activeServer.serverData.getPlayerStrings()) {
						dzMessage.writeString(playerString);
					}
				}

			}

			dzMessage.addServer(overworldServer.getServerInfo().getName()).send();
		}
	}
}
