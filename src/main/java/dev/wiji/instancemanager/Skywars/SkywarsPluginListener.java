package dev.wiji.instancemanager.Skywars;

import com.mattmalec.pterodactyl4j.UtilizationState;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.ServerManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

		if(action.equals("GAME_START")) {
			SkywarsGameManager.startGame(serverID);
		}

		if(action.equals("GAME_END")) {
			SkywarsGameManager.endGame(serverID);
		}

	}

	@EventHandler
	public void onJoin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();

		new ProxyRunnable() {
			@Override
			public void run() {
				SkywarsQueueManager.queue(player);
			}
		}.runAfter(5, TimeUnit.SECONDS);
	}
}
