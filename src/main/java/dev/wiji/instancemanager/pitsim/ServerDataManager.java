package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.objects.*;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

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
			for(MainGamemodeServer server : MainGamemodeServerManager.mixedServerList) {
				if(server.getServerInfo().getName().equals(serverName)) {
					strings.remove(0);
					strings.remove(0);
					server.serverData = new ServerData(strings);
				}
			}
		}
	}

	public static void sendServerData() {

		List<MainGamemodeServerManager> managers = new ArrayList<>();
		managers.add(MainGamemodeServerManager.getManager(ServerType.OVERWORLD));
		managers.add(MainGamemodeServerManager.getManager(ServerType.DARKZONE));

		for(MainGamemodeServerManager manager : managers) {
			for(MainGamemodeServer mainGamemodeServer : MainGamemodeServerManager.mixedServerList) {
				if(!mainGamemodeServer.status.isOnline()) continue;

				ServerType type = manager.serverType;

				PluginMessage message = new PluginMessage();
				message.writeString((type == ServerType.DARKZONE ? "DARKZONE " : "") + "SERVER DATA");

				for(MainGamemodeServer activeServer : manager.serverList) {
					message.writeInt(activeServer.serverData == null ? 0 : activeServer.serverData.getPlayerStrings().size());
					message.writeBoolean(activeServer.status == ServerStatus.RUNNING);

					if(activeServer.serverData != null) {
						for(String playerString : activeServer.serverData.getPlayerStrings()) {
							message.writeString(playerString);
						}
					}

				}

				message.addServer(mainGamemodeServer.getServerInfo().getName()).send();
			}
		}
	}
}
