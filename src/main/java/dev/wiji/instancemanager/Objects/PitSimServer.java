package dev.wiji.instancemanager.Objects;

import com.mattmalec.pterodactyl4j.UtilizationState;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.PitSim.PitSimServerManager;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.ServerManager;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PitSimServer {
	private String pteroID;
	private static int nextIndex = 1;
	private int serverIndex;

	public boolean isOnStartCooldown = false;

	private long startTime;

	public ServerStatus status = ServerStatus.OFFLINE;

	public ServerData serverData;

	public PitSimServer(String pteroID) {
		this.pteroID = pteroID;
		this.serverIndex = nextIndex++;
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
		return BungeeMain.INSTANCE.getProxy().getServerInfo("pitsim-" + serverIndex);
	}

	public List<ProxiedPlayer> getPlayers() {
		return new ArrayList<>(BungeeMain.INSTANCE.getProxy().getServerInfo("pitsim-" + serverIndex).getPlayers());
	}

	public void beginStartCooldown() {
		isOnStartCooldown = true;
		((ProxyRunnable) () -> {
			isOnStartCooldown = false;
		}).runAfter(20, TimeUnit.SECONDS);
	}
}
