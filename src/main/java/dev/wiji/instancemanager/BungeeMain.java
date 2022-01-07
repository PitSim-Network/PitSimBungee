package dev.wiji.instancemanager;


import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class BungeeMain extends Plugin implements Listener {
	@Override
	public void onEnable() {
		this.getProxy().registerChannel("BungeeCord");
		getProxy().getPluginManager().registerListener(this, this);
		for(String channel : this.getProxy().getChannels()) {
			getLogger().info(channel);
		}
	}

	@Override
	public void onDisable() {
		//make sure to unregister the registered channels in case of a reload
		this.getProxy().unregisterChannel("BungeeCord");
	}

	@EventHandler
	public void onMessage(PluginMessageEvent event) throws IOException {
		if(!event.getTag().equals("BungeeCord")) return;
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(event.getData()));

		String type = dis.readUTF();
		System.out.println("Type: " + type);
		String server = dis.readUTF();
		System.out.println("Server: " + server);
		String subChannel = dis.readUTF();
		System.out.println("Sub-Channel: " + subChannel);

		short len = dis.readShort();
		byte[] msgbytes = new byte[len];
		dis.readFully(msgbytes);
		DataInputStream subDIS = new DataInputStream(new ByteArrayInputStream(msgbytes));

		System.out.println("Game: " + subDIS.readUTF());
		System.out.println("Player: " + subDIS.readUTF());

//		while(subDIS.available() > 0) {
//			String k = subDIS.readUTF();
//			System.out.println(k);
//		}
	}
}
