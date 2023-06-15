package net.pitsim.bungee.objects;

import net.pitsim.bungee.ServerManager;

import java.util.List;
import java.util.Map;

public enum ServerType {
	OVERWORLD(OverworldServer.class, OverworldServer.serverList, ServerManager.pitSimServers),
	DARKZONE(DarkzoneServer.class, DarkzoneServer.serverList, ServerManager.darkzoneServers);

	private final Class<? extends PitSimServer> serverClass;
	private final List<PitSimServer> serverList;
	private final Map<String, String> serverStrings;

	ServerType(Class<? extends PitSimServer> serverClass, List<PitSimServer> serverList, Map<String, String> serverStrings) {
		this.serverClass = serverClass;
		this.serverList = serverList;
		this.serverStrings = serverStrings;
	}

	public Class<? extends PitSimServer> getServerClass() {
		return serverClass;
	}

	public List<PitSimServer> getServerList() {
		return serverList;
	}

	public Map<String, String> getServerStrings() {
		return serverStrings;
	}
}
