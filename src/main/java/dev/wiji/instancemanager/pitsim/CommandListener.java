package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.MainGamemodeServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Locale;

public class CommandListener implements Listener {

	public static final int COOLDOWN_SECONDS = 3;

	@EventHandler
	public void onCommandSend(ChatEvent event) {
		if(!event.isCommand()) return;
		if(!(event.getSender() instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();

		if(MainGamemodeServer.cooldownPlayers.containsKey(player)) {
			long time = MainGamemodeServer.cooldownPlayers.get(player);

			if(time + COOLDOWN_SECONDS * 1000 < System.currentTimeMillis()) {
				MainGamemodeServer.cooldownPlayers.remove(player);
			} else {
				if(event.getMessage().toLowerCase().startsWith("/play")) return;
				event.setCancelled(true);
				AOutput.error(player, "You may not use that command at this time.");
				return;
			}
		}


		if(MainGamemodeServer.guildCooldown.containsKey(player)) {
			long time = MainGamemodeServer.guildCooldown.get(player);

			if(time + COOLDOWN_SECONDS * 1000 < System.currentTimeMillis()) {
				MainGamemodeServer.guildCooldown.remove(player);
			} else {
				if(event.getMessage().toLowerCase().startsWith("/play")) return;
				event.setCancelled(true);
				AOutput.error(player, "You may not use that command at this time.");
				return;
			}
		}
	}
}
