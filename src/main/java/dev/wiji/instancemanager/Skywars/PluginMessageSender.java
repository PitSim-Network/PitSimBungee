package dev.wiji.instancemanager.Skywars;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PluginMessageSender {

	public static void sendPlayerStats() {

		new ProxyRunnable() {
			@Override
			public void run() {
				sendInfo();
				sendHubInfo();
			}
		}.runAfterEvery(5, 5, TimeUnit.SECONDS);
	}

	public static ProxiedPlayer getPlayer() {
		Collection<ProxiedPlayer> players = BungeeMain.INSTANCE.getProxy().getServerInfo("skywars").getPlayers();

		if(players.size() == 0) return null;
		else return players.iterator().next();
 	}

	public static ProxiedPlayer getHubPlayer() {
		Collection<ProxiedPlayer> players = BungeeMain.INSTANCE.getProxy().getServerInfo("lobby").getPlayers();

		if(players.size() == 0) return null;
		else return players.iterator().next();
	}


	public static int getPlayers() {
		int players = 0;
		players = players + SkywarsGameManager.mainQueuePlayers;
		players = players + SkywarsGameManager.backupQueuePlayers;

	    for(String s : SkywarsGameManager.activeServers.keySet()) {
		    players = players + BungeeMain.INSTANCE.getProxy().getServerInfo(s).getPlayers().size();
	    }
	    return players;
    }

    public static int getServers() {
		int servers = 0;

		servers = servers + SkywarsGameManager.activeServers.size();
		if(SkywarsGameManager.mainQueuePlayers != 0) servers++;
		if(SkywarsGameManager.backupQueuePlayers != 0) servers++;
		return servers;
    }

	public static void sendInfo() {

		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Forward"); // So BungeeCord knows to forward it
		out.writeUTF("skywars");
		out.writeUTF("SkywarsHub");

		ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
		DataOutputStream msgout = new DataOutputStream(msgbytes);
		try {
			msgout.writeUTF(String.valueOf(getPlayers()));
			msgout.writeUTF(String.valueOf(getServers()));
		} catch (IOException exception){
			exception.printStackTrace();
		}

		out.writeShort(msgbytes.toByteArray().length);
		out.write(msgbytes.toByteArray());

		if(getPlayer() != null) getPlayer().getServer().sendData("BungeeCord", out.toByteArray());

	}

	public static void sendHubInfo() {

		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Forward"); // So BungeeCord knows to forward it
		out.writeUTF("lobby");
		out.writeUTF("SkywarsHub");

		ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
		DataOutputStream msgout = new DataOutputStream(msgbytes);
		try {
			msgout.writeUTF(String.valueOf(getPlayers()));
			msgout.writeUTF(String.valueOf(getServers()));
		} catch (IOException exception){
			exception.printStackTrace();
		}

		out.writeShort(msgbytes.toByteArray().length);
		out.write(msgbytes.toByteArray());

		if(getHubPlayer() != null) getHubPlayer().getServer().sendData("BungeeCord", out.toByteArray());

	}

}
