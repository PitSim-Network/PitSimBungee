package dev.wiji.instancemanager.Skywars;

import dev.wiji.instancemanager.BungeeMain;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SkywarsQueueManager {

	public static int maxGameSize = 5;
	public static int minGameSize = 2;

	public static void queue(ProxiedPlayer player) {
		try {
			SkywarsGameManager.mainQueuePlayers = BungeeMain.INSTANCE.getProxy().getServerInfo(SkywarsGameManager.mainQueueServer).getPlayers().size();
		} catch(Exception e) { SkywarsGameManager.mainQueuePlayers = 0; }

		try {
			SkywarsGameManager.backupQueuePlayers = BungeeMain.INSTANCE.getProxy().getServerInfo(SkywarsGameManager.backupQueueServer).getPlayers().size();
		} catch(Exception e) { SkywarsGameManager.backupQueuePlayers = 0; }

		String targetServer = null;

//		System.out.println(SkywarsGameManager.mainQueueServer);
//		System.out.println(SkywarsGameManager.mainQueuePlayers);
//		System.out.println(SkywarsGameManager.backupQueueServer);
//		System.out.println(SkywarsGameManager.backupQueuePlayers);

		if(SkywarsGameManager.mainQueuePlayers < maxGameSize) {
			targetServer = SkywarsGameManager.mainQueueServer;
		} else if(SkywarsGameManager.backupQueuePlayers < maxGameSize) {
			targetServer = SkywarsGameManager.backupQueueServer;
		}

		if(targetServer == null) {
			player.disconnect(new ComponentBuilder("There are currently no available servers. Please try again in a moment!").color(ChatColor.RED).create());
		} else {
			System.out.println(player.getServer().getInfo().getName());
			if (player.getServer().getInfo().getName().equalsIgnoreCase(targetServer)) {
				player.sendMessage(new ComponentBuilder("You are already connected to that server!").color(ChatColor.RED).create());
			} else {
				ServerInfo target = ProxyServer.getInstance().getServerInfo(targetServer);
				player.connect(target);
			}
		}

		try {
			SkywarsGameManager.mainQueuePlayers = BungeeMain.INSTANCE.getProxy().getServerInfo(SkywarsGameManager.mainQueueServer).getPlayers().size();
		} catch(Exception e) { SkywarsGameManager.mainQueuePlayers = 0; }

		try {
			SkywarsGameManager.backupQueuePlayers = BungeeMain.INSTANCE.getProxy().getServerInfo(SkywarsGameManager.backupQueueServer).getPlayers().size();
		} catch(Exception e) { SkywarsGameManager.backupQueuePlayers = 0; }

		if(SkywarsGameManager.mainQueuePlayers >= (minGameSize - 1) && SkywarsGameManager.backupQueueServer == null && SkywarsGameManager.startingServers.size() == 0) {
			SkywarsGameManager.fetchServer();
		}


	}

}
