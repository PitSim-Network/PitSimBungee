package dev.wiji.instancemanager.Skywars;

import dev.wiji.instancemanager.ServerManager;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class SkywarsPluginListener implements Listener {

	@EventHandler
	public void onMessage(PluginMessageEvent event) throws IOException {
		System.out.println("Message recieved!");
		if(!event.getTag().equals("BungeeCord")) return;
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(event.getData()));

		String type = dis.readUTF();
		String server = dis.readUTF();
		String subChannel = dis.readUTF();

		if(!subChannel.equals("Skywars")) return;

		short len = dis.readShort();
		byte[] msgbytes = new byte[len];
		dis.readFully(msgbytes);
		DataInputStream subDIS = new DataInputStream(new ByteArrayInputStream(msgbytes));

		String serverID = subDIS.readUTF();
		String action = subDIS.readUTF();
		String argument = subDIS.readUTF();
		String player = subDIS.readUTF();

		if(action.equals("PLAYERCOUNT")) {
			if(serverID.equals(SkywarsGameManager.mainQueueServer)) SkywarsGameManager.mainQueuePlayers = Integer.parseInt(argument);
			if(serverID.equals(SkywarsGameManager.backupQueueServer)) SkywarsGameManager.backupQueuePlayers = Integer.parseInt(argument);

			System.out.println(SkywarsGameManager.mainQueuePlayers);
			System.out.println(SkywarsGameManager.backupQueuePlayers);
		}

//
	}
}
