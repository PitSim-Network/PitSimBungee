package dev.wiji.instancemanager.objects;

import com.mattmalec.pterodactyl4j.UtilizationState;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.ServerManager;
import dev.wiji.instancemanager.storage.StorageProfile;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class MainServer {

	public static List<MainServer> serverList = new ArrayList<>();

	public final String pteroID;
	public final int serverIndex;
	public boolean isOnStartCooldown = false;
	public long startTime;
	public ServerStatus status = ServerStatus.OFFLINE;

	public final ServerType serverType;
	public ServerData serverData;

	private List<StorageProfile> loadedProfiles = new ArrayList<>();

	public MainServer(String pteroID, ServerType serverType, int serverIndex) {
		this.pteroID = pteroID;
		this.serverIndex = serverIndex;

		this.serverType = serverType;
		serverList.add(this);
	}

	public void startUp() {
		ServerManager.startServer(pteroID);
	}

	public UtilizationState getState() {
		return ServerManager.getState(pteroID);
	}

	public void shutDown(boolean restart) {
		new PluginMessage().writeString("SHUTDOWN").writeBoolean(restart).addServer(getServerInfo()).send();
		if(restart) status = ServerStatus.RESTARTING_INITIAL;
		else status = ServerStatus.SHUTTING_DOWN_INITIAL;
	}

	public void hardShutDown() {
		ServerManager.stopServer(pteroID);
		status = ServerStatus.OFFLINE;
	}

	public String getPteroID() {
		return pteroID;
	}

	public int getServerIndex() {
		return serverIndex;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public ServerInfo getServerInfo() {
		if(serverType == ServerType.DARKZONE) return BungeeMain.INSTANCE.getProxy().getServerInfo("darkzone-" + serverIndex);
		else return BungeeMain.INSTANCE.getProxy().getServerInfo("pitsim-" + serverIndex);
	}

	public List<ProxiedPlayer> getPlayers() {
		if(serverType == ServerType.DARKZONE) return new ArrayList<>(BungeeMain.INSTANCE.getProxy().getServerInfo("darkzone-" + serverIndex).getPlayers());
		else return new ArrayList<>(BungeeMain.INSTANCE.getProxy().getServerInfo("pitsim-" + serverIndex).getPlayers());

	}

	public void beginStartCooldown() {
		isOnStartCooldown = true;
		((ProxyRunnable) () -> {
			isOnStartCooldown = false;
		}).runAfter(20, TimeUnit.SECONDS);
	}

	public static MainServer getServer(ServerInfo info) {
		for(MainServer mainServer : serverList) {
			if(mainServer.getServerInfo() == info) return mainServer;
		}
		return null;
	}

	public static MainServer getLoadedServer(StorageProfile profile) {
		for(MainServer mainServer : serverList) {
			if(mainServer.loadedProfiles.contains(profile)) return mainServer;
		}
		return null;
	}

	public void removeProfile(StorageProfile profile) {
		if(profile != null) loadedProfiles.remove(profile);
	}

	public List<StorageProfile> getLoadedProfiles() {
		return loadedProfiles;
	}

	public void addProfile(StorageProfile profile) {
		loadedProfiles.add(profile);
	}
}

enum ServerType {
	PITSIM,
	DARKZONE;
}
