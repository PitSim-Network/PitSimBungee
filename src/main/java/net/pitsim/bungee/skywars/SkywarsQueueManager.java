package net.pitsim.bungee.skywars;

import net.pitsim.bungee.BungeeMain;
import net.pitsim.bungee.ProxyRunnable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SkywarsQueueManager {

	public static int maxGameSize = 12;
	public static int minGameSize = 3;

	private static boolean alertCooldown = false;

	public static void queue(ProxiedPlayer player) {
		try {
			SkywarsGameManager.mainQueuePlayers = BungeeMain.INSTANCE.getProxy().getServerInfo(SkywarsGameManager.mainQueueServer).getPlayers().size();
		} catch(Exception e) {
			SkywarsGameManager.mainQueuePlayers = 0;
		}

		try {
			SkywarsGameManager.backupQueuePlayers = BungeeMain.INSTANCE.getProxy().getServerInfo(SkywarsGameManager.backupQueueServer).getPlayers().size();
		} catch(Exception e) {
			SkywarsGameManager.backupQueuePlayers = 0;
		}

		String targetServer = null;

//		System.out.println(SkywarsGameManager.mainQueueServer);
//		System.out.println(SkywarsGameManager.mainQueuePlayers);
//		System.out.println(SkywarsGameManager.backupQueueServer);
//	`	System.out.println(SkywarsGameManager.backupQueuePlayers);

		if(SkywarsGameManager.mainQueuePlayers < maxGameSize) {
			targetServer = SkywarsGameManager.mainQueueServer;
		} else if(SkywarsGameManager.backupQueuePlayers < maxGameSize) {
			targetServer = SkywarsGameManager.backupQueueServer;
		}

		if(targetServer == null) {
			player.sendMessage(new ComponentBuilder("There are currently no available servers. Please try again in a moment!").color(ChatColor.RED).create());
			return;
		} else {
//			System.out.println(player.getServer().getInfo().getName());
			if(player.getServer().getInfo().getName().equalsIgnoreCase(targetServer)) {
				player.sendMessage(new ComponentBuilder("You are already connected to that server!").color(ChatColor.RED).create());
			} else {
				ServerInfo target = ProxyServer.getInstance().getServerInfo(targetServer);
				System.out.println(SkywarsGameManager.mainQueueServer);
				System.out.println(targetServer);
				player.connect(target);
			}
		}

		try {
			SkywarsGameManager.mainQueuePlayers = BungeeMain.INSTANCE.getProxy().getServerInfo(SkywarsGameManager.mainQueueServer).getPlayers().size();
		} catch(Exception e) {
			SkywarsGameManager.mainQueuePlayers = 0;
		}

		try {
			SkywarsGameManager.backupQueuePlayers = BungeeMain.INSTANCE.getProxy().getServerInfo(SkywarsGameManager.backupQueueServer).getPlayers().size();
		} catch(Exception e) {
			SkywarsGameManager.backupQueuePlayers = 0;
		}

		if(SkywarsGameManager.mainQueuePlayers >= 0 && SkywarsGameManager.backupQueueServer == null && SkywarsGameManager.startingServers.size() == 0) {
			SkywarsGameManager.fetchServer();
		}

		if(SkywarsGameManager.mainQueuePlayers >= 2 || SkywarsGameManager.backupQueuePlayers >= 2) {
			if(!alertCooldown) return;

			for(ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {

				String server = proxiedPlayer.getServer().getInfo().getName();
				if(SkywarsGameManager.activeServers.containsKey(server)) continue;
				if(Objects.equals(SkywarsGameManager.mainQueueServer, server)) continue;
				if(Objects.equals(SkywarsGameManager.backupQueueServer, server)) continue;

				proxiedPlayer.sendMessage(new ComponentBuilder("-------------------------").color(ChatColor.DARK_GRAY).strikethrough(true).create());
				proxiedPlayer.sendMessage(new ComponentBuilder("A Skywars game is starting soon!").color(ChatColor.LIGHT_PURPLE).bold(true).create());
				proxiedPlayer.sendMessage(new ComponentBuilder("Use ").color(ChatColor.YELLOW).append("/play skywars ").color(ChatColor.WHITE).append("to join!").color(ChatColor.YELLOW).create());
				proxiedPlayer.sendMessage(new ComponentBuilder("-------------------------").color(ChatColor.DARK_GRAY).strikethrough(true).create());
			}

			alertCooldown = true;
			((ProxyRunnable) () -> alertCooldown = false).runAfter(60, TimeUnit.SECONDS);

		}

	}

}
