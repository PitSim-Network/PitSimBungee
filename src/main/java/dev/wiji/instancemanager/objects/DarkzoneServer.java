package dev.wiji.instancemanager.objects;

import com.mattmalec.pterodactyl4j.UtilizationState;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.ServerManager;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DarkzoneServer extends MainServer {

	private static int NEXT_INDEX = 1;

	public DarkzoneServer(String pteroID) {
		super(pteroID, ServerType.DARKZONE, NEXT_INDEX++);
	}
}
