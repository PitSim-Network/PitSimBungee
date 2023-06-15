package net.pitsim.bungee.pitsim;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.pitsim.bungee.ProxyRunnable;
import net.pitsim.bungee.events.MessageEvent;
import net.pitsim.bungee.objects.*;

import java.util.ArrayList;
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
			for(PitSimServer server : PitSimServerManager.mixedServerList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					strings.remove(0);
					strings.remove(0);
					server.serverData = new ServerData(strings);
				}
			}
		}
	}

	public static void sendServerData() {

		List<PitSimServerManager> managers = new ArrayList<>();
		managers.add(PitSimServerManager.getManager(ServerType.OVERWORLD));
		managers.add(PitSimServerManager.getManager(ServerType.DARKZONE));

		for(PitSimServerManager manager : managers) {
			for(PitSimServer pitSimServer : PitSimServerManager.mixedServerList) {
				if(!pitSimServer.status.isOnline()) continue;

				ServerType type = manager.serverType;

				PluginMessage message = new PluginMessage();
				message.writeString((type == ServerType.DARKZONE ? "DARKZONE " : "") + "SERVER DATA");

				for(PitSimServer activeServer : manager.serverList) {
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
		}
	}
}
