package dev.wiji.instancemanager.Objects;

import com.mattmalec.pterodactyl4j.UtilizationState;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.PitSim.PitSimServerManager;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.ServerManager;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PitSimServer {

    private String pteroID;
    private int serverIndex;

    private long startTime;

    public PitSimServer(String pteroID, int serverIndex) {
        this.pteroID = pteroID;
        this.serverIndex = serverIndex;
    }

    public void startUp(boolean alreadyStarting) {
        if(!alreadyStarting) ServerManager.startServer(pteroID);

        ((ProxyRunnable) () -> {
            if(getState() == UtilizationState.RUNNING) {
                PitSimServerManager.activeServers.add(this);
                startTime = System.currentTimeMillis();
            } else {
                System.out.println("Server " + pteroID + " failed to start up!");
            }
        }).runAfter(30, TimeUnit.SECONDS);
    }

    public UtilizationState getState() {
        return ServerManager.getState(pteroID);
    }

    public void shutDown() {
        new PluginMessage().writeString("SHUTDOWN").addServer(getServerInfo()).send();
    }

    public void hardShutDown() {
        ServerManager.stopServer(pteroID);
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


}
