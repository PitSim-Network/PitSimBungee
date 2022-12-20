package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.MainGamemodeServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandListener implements Listener {
	@EventHandler
	public void onCommandSend(ChatEvent event) {
		if(!event.isCommand()) return;
		if(!(event.getSender() instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		if(MainGamemodeServer.cooldownPlayers.contains(player)) {
			event.setCancelled(true);
			AOutput.error(player, "You may not use that command at this time.");
		}
	}
}
