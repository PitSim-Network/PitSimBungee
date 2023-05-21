package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.commands.BroadcastCommand;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

public class CrossServerMessageManager implements Listener {

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();
		List<Integer> integers = message.getIntegers();
		List<Long> longs = message.getLongs();
		List<Boolean> booleans = message.getBooleans();
		if(strings.isEmpty()) return;

		if(strings.get(0).equals("ITEMSHOW") || strings.get(0).equals("FINDJEWEL") || strings.get(0).equals("UBERDROP") || strings.get(0).equals("TAINTEDENCHANT")) {
			String serverName = strings.get(1);
			String displayName = strings.get(2);
			String itemStack = strings.get(3);
			PluginMessage pluginMessage = new PluginMessage()
					.writeString(strings.get(0))
					.writeString(displayName)
					.writeString(itemStack);
			for(PitSimServer server : PitSimServerManager.mixedServerList) {
				if(!server.status.isOnline() || server.getServerInfo().getName().equals(serverName)) continue;
				pluginMessage.addServer(server.getServerInfo());
			}
			pluginMessage.send();
		} else if(strings.get(0).equals("PRESTIGE")) {
			String serverName = strings.get(1);
			String displayName = strings.get(2);
			int prestige = integers.get(0);
			PluginMessage pluginMessage = new PluginMessage()
					.writeString(strings.get(0))
					.writeString(displayName)
					.writeInt(prestige);
			for(PitSimServer server : PitSimServerManager.mixedServerList) {
				if(!server.status.isOnline() || server.getServerInfo().getName().equals(serverName)) continue;
				pluginMessage.addServer(server.getServerInfo());
			}
			pluginMessage.send();
		} else if(strings.get(0).equals("JUDGEMENT")) {
			String serverName = strings.get(1);
			String playerUUID = strings.get(2);
			PluginMessage pluginMessage = new PluginMessage()
					.writeString(strings.get(0))
					.writeString(playerUUID);
			for(PitSimServer server : PitSimServerManager.mixedServerList) {
				if(!server.status.isOnline() || server.getServerInfo().getName().equals(serverName)) continue;
				pluginMessage.addServer(server.getServerInfo());
			}
			pluginMessage.send();
		} else if(strings.get(0).equals("BROADCAST")) {
			BroadcastCommand.broadcast(strings.get(1));
		} else if(strings.get(0).equals("AUCTIONREQUEST")) {
			String serverName = strings.get(1);
			PluginMessage forwardMessage = new PluginMessage()
					.writeString(strings.get(0))
					.writeString(serverName);

			PitSimServerManager darkzoneManager = PitSimServerManager.getManager(ServerType.DARKZONE);
			assert darkzoneManager != null;
			for(PitSimServer server : darkzoneManager.serverList) {
				if(!server.status.isOnline()) continue;
				forwardMessage.addServer(server.getServerInfo());
				AOutput.log("Received request for darkzone data. forwarding to " + server.getServerInfo().getName());
				break;
			}
			forwardMessage.send();
		} else if(strings.get(0).equals("AUCTIONDATA")) {
			String serverName = strings.get(1);
			long timeRemaining = longs.get(0);
			PluginMessage forwardMessage = new PluginMessage()
					.writeString(strings.get(0))
					.writeLong(timeRemaining);

			strings.remove(0);
			strings.remove(0);
			for(String string : strings) forwardMessage.writeString(string);
			for(int integer : integers) forwardMessage.writeInt(integer);

			PitSimServerManager overworldManager = PitSimServerManager.getManager(ServerType.OVERWORLD);
			assert overworldManager != null;
			for(PitSimServer server : overworldManager.serverList) {
				if(!server.status.isOnline()) continue;
				if(!serverName.isEmpty() && !server.getServerInfo().getName().equals(serverName)) continue;
				forwardMessage.addServer(server.getServerInfo());
				AOutput.log("Received darkzone data. forwarding to " + server.getServerInfo().getName());
			}
			forwardMessage.send();
		}
	}
}
