package dev.wiji.instancemanager.skywars;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SkywarsPluginListener implements Listener {

	public static List<String> startedServers = new ArrayList<>();

	@EventHandler
	public void onMessage(PluginMessageEvent event) throws IOException {
		if(event.getSender() instanceof ProxiedPlayer) {
			Server server = ((ProxiedPlayer) event.getSender()).getServer();
		}

		try {
			if(!event.getTag().equals("BungeeCord")) return;
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(event.getData()));

			String type = dis.readUTF();
			String server = dis.readUTF();
			String subChannel = null;

			try {
				subChannel = dis.readUTF();
			} catch(Exception e) {
				return;
			}

			if(!subChannel.equals("Skywars")) return;

			short len = dis.readShort();
			byte[] msgbytes = new byte[len];
			dis.readFully(msgbytes);
			DataInputStream subDIS = new DataInputStream(new ByteArrayInputStream(msgbytes));

			String serverID = subDIS.readUTF();
			String action = subDIS.readUTF();
			String argument = subDIS.readUTF();
			String player = subDIS.readUTF();

			if(action.equals("GAME_START")) {
				if(startedServers.contains(serverID)) return;
				SkywarsGameManager.startGame(serverID);
				System.out.println("Skywars game started on " + serverID);
				startedServers.add(serverID);

				new ProxyRunnable() {
					@Override
					public void run() {
						startedServers.remove(serverID);
					}
				}.runAfter(10, TimeUnit.SECONDS);
			}



		if(action.equals("GAME_END")) {
			SkywarsGameManager.endGame(serverID);
			System.out.println("Skywars game ended on " + serverID);
			checkForServerWait();
		}
		} catch(Exception ignored) { }
	}

	@EventHandler
	public void onPlayCommand(PluginMessageEvent event) throws IOException {

		try {
			if(!event.getTag().equals("BungeeCord")) return;
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(event.getData()));

			String type = dis.readUTF();
			String server = dis.readUTF();
			String subChannel = null;

			try {
				subChannel = dis.readUTF();
			} catch(Exception e) {
				return;
			}

			if(!subChannel.equals("SkywarsQueue")) return;

			short len = dis.readShort();
			byte[] msgbytes = new byte[len];
			dis.readFully(msgbytes);
			DataInputStream subDIS = new DataInputStream(new ByteArrayInputStream(msgbytes));

			String playerString = subDIS.readUTF();
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerString);

			SkywarsQueueManager.queue((ProxiedPlayer) player);
//		if(action.equals("GAME_END")) {
//			SkywarsGameManager.endGame(serverID);
//			System.out.println("Skywars game ended on " + serverID);
//		}
		} catch(Exception ignored) { }
	}



	@EventHandler
	public void onSwitch(ServerSwitchEvent event) {
		for(Map.Entry<String, ScheduledTask> entry : SkywarsGameManager.activeServers.entrySet()) {
			int size = BungeeMain.INSTANCE.getProxy().getServerInfo(entry.getKey()).getPlayers().size();
			if(size == 0) {
				SkywarsGameManager.endGame(entry.getKey());
				System.out.println("Skywars game ended on " + entry.getKey());
				checkForServerWait();
			}
		}
	}

	@EventHandler
	public void onSwitch(ServerDisconnectEvent event) {
		for(Map.Entry<String, ScheduledTask> entry : SkywarsGameManager.activeServers.entrySet()) {
			int size = BungeeMain.INSTANCE.getProxy().getServerInfo(entry.getKey()).getPlayers().size();
			if(size == 0) {
				SkywarsGameManager.endGame(entry.getKey());
				System.out.println("Skywars game ended on " + entry.getKey());
				checkForServerWait();
			}
		}
	}

	@EventHandler
	public void onLeave(PlayerDisconnectEvent event) {
		for(Map.Entry<String, ScheduledTask> entry : SkywarsGameManager.activeServers.entrySet()) {
//			System.out.println(entry.getKey());
			int size = BungeeMain.INSTANCE.getProxy().getServerInfo(entry.getKey()).getPlayers().size();
//			System.out.println(size);
			if(size == 1) {
				SkywarsGameManager.endGame(entry.getKey());
				System.out.println("Skywars game ended on " + entry.getKey());
				checkForServerWait();
			}
		}
	}

	public static void checkForServerWait() {
		if(!SkywarsGameManager.outOfGameServers) return;

		SkywarsGameManager.fetchServer();
		SkywarsGameManager.outOfGameServers = false;
	}
}
