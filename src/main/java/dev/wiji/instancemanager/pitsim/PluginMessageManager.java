package dev.wiji.instancemanager.pitsim;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryMessageListener;
import septogeddon.pluginquery.bungeecord.BungeePluginQuery;

import java.io.*;
import java.util.Collection;

public class PluginMessageManager implements QueryMessageListener, Listener {
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
			msgout.writeInt(message.getLongs().size());
			msgout.writeInt(message.getBooleans().size());

			for(String string : message.getStrings()) {
				msgout.writeUTF(string == null ? "" : string);
			}

			for(int integer : message.getIntegers()) {
				msgout.writeInt(integer);
			}

			for(long longValue : message.getLongs()) {
				msgout.writeLong(longValue);
			}

			for(Boolean bool : message.getBooleans()) {
				msgout.writeBoolean(bool);
			}

		} catch(IOException exception) {
			exception.printStackTrace();
		}

		out.writeInt(msgbytes.toByteArray().length);
		out.write(msgbytes.toByteArray());

		ProxiedPlayer player = getPlayer(server.getName());
		assert player != null;

		QueryConnection connection = BungeePluginQuery.getConnection(server);
		if (connection != null) {
			connection.sendQuery("BungeeCord", out.toByteArray());
		} else {
			throw new IllegalArgumentException("server is not yet connected");
		}
//		player.getServer().sendData("BungeeCord", out.toByteArray());

		lastMessage = message;

	}

	public static ProxiedPlayer getPlayer(String server) {
		Collection<ProxiedPlayer> players = BungeeMain.INSTANCE.getProxy().getServerInfo(server).getPlayers();

		if(players.size() == 0) return null;
		else return players.iterator().next();
	}

	@Override
	public void onQueryReceived(QueryConnection connection, String channel, byte[] message) {
		if(channel.equals("BungeeCord")) {
			try {

				ByteArrayDataInput in = ByteStreams.newDataInput(message);
				String type = in.readUTF();
				String server = in.readUTF();
				String subChannel = in.readUTF();

				if(!subChannel.equals("PitSim")) return;

				int len = in.readInt();
				byte[] msgbytes = new byte[len];
				in.readFully(msgbytes);
				DataInputStream subDIS = new DataInputStream(new ByteArrayInputStream(msgbytes));

				PluginMessage pluginMessage = new PluginMessage(subDIS);
				BungeeMain.INSTANCE.getProxy().getPluginManager().callEvent(new MessageEvent(pluginMessage, subChannel));

			} catch(Exception e) {
				e.printStackTrace();
			}
		}

	}

}
