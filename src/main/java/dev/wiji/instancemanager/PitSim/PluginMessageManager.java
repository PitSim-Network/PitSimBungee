package dev.wiji.instancemanager.PitSim;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Events.MessageEvent;
import dev.wiji.instancemanager.Objects.PluginMessage;
import dev.wiji.instancemanager.ProxyRunnable;
import dev.wiji.instancemanager.Skywars.SkywarsGameManager;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class PluginMessageManager implements Listener {

	public static PluginMessage lastMessage;


	public static void sendMessage(PluginMessage message, ServerInfo server) {

		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Forward"); // So BungeeCord knows to forward it
		out.writeUTF(server.getName());
		out.writeUTF("PitSim");

		ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
		DataOutputStream msgout = new DataOutputStream(msgbytes);
		try {
			msgout.writeUTF(message.messageID.toString());
			msgout.writeUTF(message.responseID.toString());
			msgout.writeUTF(server.getName());

			msgout.writeInt(message.getStrings().size());
			msgout.writeInt(message.getIntegers().size());
			msgout.writeInt(message.getBooleans().size());

			for(String string : message.getStrings()) {
				msgout.writeUTF(string);
			}
			for(int integer : message.getIntegers()) {
				msgout.writeInt(integer);
			}

			for(Boolean bool : message.getBooleans()) {
				msgout.writeBoolean(bool);
			}

		} catch(IOException exception) {
			exception.printStackTrace();
		}

		out.writeShort(msgbytes.toByteArray().length);
		out.write(msgbytes.toByteArray());

		ProxiedPlayer player = getPlayer(server.getName());
		assert player != null;
		player.getServer().sendData("BungeeCord", out.toByteArray());

		lastMessage = message;

	}

	public static ProxiedPlayer getPlayer(String server) {
		Collection<ProxiedPlayer> players = BungeeMain.INSTANCE.getProxy().getServerInfo(server).getPlayers();

		if(players.size() == 0) return null;
		else return players.iterator().next();
	}

	@EventHandler
	public void onReceive(PluginMessageEvent event) {
		if(event.getTag().equals("BungeeCord")) {
			try {
				DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
				String type = in.readUTF();
				String server = in.readUTF();
				String subChannel = in.readUTF();

				if(!subChannel.equals("PitSim")) return;

				short len = in.readShort();
				byte[] msgbytes = new byte[len];
				in.readFully(msgbytes);
				DataInputStream subDIS = new DataInputStream(new ByteArrayInputStream(msgbytes));

				PluginMessage pluginMessage = new PluginMessage(subDIS);
				BungeeMain.INSTANCE.getProxy().getPluginManager().callEvent(new MessageEvent(pluginMessage, subChannel));

			} catch(Exception e) { }
		}

	}

}
