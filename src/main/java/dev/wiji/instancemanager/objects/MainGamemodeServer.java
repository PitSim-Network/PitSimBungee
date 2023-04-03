package dev.wiji.instancemanager.objects;

import com.mattmalec.pterodactyl4j.UtilizationState;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.ServerManager;
import dev.wiji.instancemanager.storage.StorageProfile;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class MainGamemodeServer {

	public static List<MainGamemodeServer> serverList = new ArrayList<>();
	public static Map<UUID, Long> cooldownPlayers = new HashMap<>();

	public static Map<UUID, Long> guildCooldown = new HashMap<>();

	public final String pteroID;
	public final int serverIndex;
	public boolean isOnStartCooldown = false;
	public long startTime;
	public ServerStatus status = ServerStatus.OFFLINE;
	public ServerStatus suspendedStatus = null;

	public boolean staffOverride = false;

	public final ServerType serverType;
	public ServerData serverData;

	private List<StorageProfile> loadedProfiles = new ArrayList<>();

	public MainGamemodeServer(String pteroID, ServerType serverType, int serverIndex) {
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
		shutDown(restart, -1);
	}

	public void shutDown(boolean restart, int minutes) {
		PluginMessage message = new PluginMessage().writeString("SHUTDOWN").writeBoolean(restart).addServer(getServerInfo());
		if(minutes > 0) message.writeInt(minutes);
		message.send();
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
		if(serverType == ServerType.DARKZONE) {
			if(ConfigManager.isDev()) return BungeeMain.INSTANCE.getProxy().getServerInfo("darkzonedev-" + serverIndex);
			return BungeeMain.INSTANCE.getProxy().getServerInfo("darkzone-" + serverIndex);
		} else {
			if(ConfigManager.isDev()) return BungeeMain.INSTANCE.getProxy().getServerInfo("pitsimdev-" + serverIndex);
			return BungeeMain.INSTANCE.getProxy().getServerInfo("pitsim-" + serverIndex);
		}
	}

	public List<ProxiedPlayer> getPlayers() {
		if(serverType == ServerType.DARKZONE) return new ArrayList<>(getServerInfo().getPlayers());
		else return new ArrayList<>(getServerInfo().getPlayers());

	}

	public boolean isSuspended() {
		return status == ServerStatus.SUSPENDED;
	}

	public void beginStartCooldown() {
		isOnStartCooldown = true;
		((ProxyRunnable) () -> {
			isOnStartCooldown = false;
			status = ServerStatus.OFFLINE;
		}).runAfter(20, TimeUnit.SECONDS);
	}

	public static MainGamemodeServer getServer(ServerInfo info) {
		for(MainGamemodeServer mainGamemodeServer : serverList) {
			if(mainGamemodeServer.getServerInfo() == info) return mainGamemodeServer;
		}
		return null;
	}

	public static MainGamemodeServer getLoadedServer(StorageProfile profile) {
		for(MainGamemodeServer mainGamemodeServer : serverList) {
			if(mainGamemodeServer.loadedProfiles.contains(profile)) return mainGamemodeServer;
		}
		return null;
	}

	public static MainGamemodeServer getServer(int index, boolean darkzone) {
		for(MainGamemodeServer mainGamemodeServer : serverList) {
			if(mainGamemodeServer.serverIndex == index && mainGamemodeServer.serverType == (darkzone ? ServerType.DARKZONE : ServerType.PITSIM)) return mainGamemodeServer;
		}
		return null;
	}

	public void removeProfile(StorageProfile profile) {
//		System.out.println("Removed Profile: " + profile);
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
