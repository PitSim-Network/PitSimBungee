package dev.wiji.instancemanager.objects;

import dev.wiji.instancemanager.ServerManager;

import java.util.List;
import java.util.Map;

public enum ServerType {
	OVERWORLD(OverworldServer.class, OverworldServer.serverList, ServerManager.pitSimServers),
	DARKZONE(DarkzoneServer.class, DarkzoneServer.serverList, ServerManager.darkzoneServers);

	private final Class<? extends MainGamemodeServer> serverClass;
	private final List<MainGamemodeServer> serverList;
	private final Map<String, String> serverStrings;

	ServerType(Class<? extends MainGamemodeServer> serverClass, List<MainGamemodeServer> serverList, Map<String, String> serverStrings) {
		this.serverClass = serverClass;
		this.serverList = serverList;
		this.serverStrings = serverStrings;
	}

	public Class<? extends MainGamemodeServer> getServerClass() {
		return serverClass;
	}

	public List<MainGamemodeServer> getServerList() {
		return serverList;
	}

	public Map<String, String> getServerStrings() {
		return serverStrings;
	}
}
