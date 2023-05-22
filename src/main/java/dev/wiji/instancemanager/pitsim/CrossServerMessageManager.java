package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.commands.BroadcastCommand;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.objects.PitSimServer;
import dev.wiji.instancemanager.objects.PluginMessage;
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
			boolean prefix = booleans.get(0);
			BroadcastCommand.broadcast(strings.get(1), prefix);
		}
	}
}
