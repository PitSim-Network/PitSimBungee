package dev.wiji.instancemanager.PitSim;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.Events.MessageEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

public class MessageListener implements Listener {

	@EventHandler
	public void onMessage(MessageEvent event) {
		List<String> strings = event.getMessage().getStrings();
		if(strings.size() < 2) return;

		if(strings.get(0).equals("QUEUE")) {
			String playerString = strings.get(1);
			ProxiedPlayer player = BungeeMain.INSTANCE.getProxy().getPlayer(playerString);
			if(player == null) return;

			PitSimServerManager.queue(player);
		}
	}
}
