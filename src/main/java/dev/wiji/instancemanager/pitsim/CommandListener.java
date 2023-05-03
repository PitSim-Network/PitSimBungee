package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.objects.PitSimServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandListener implements Listener {

	public static final int COOLDOWN_SECONDS = 3;

	@EventHandler
	public void onCommandSend(ChatEvent event) {
		if(!event.isCommand()) return;
		if(!(event.getSender() instanceof ProxiedPlayer)) return;
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();

		if(PitSimServer.cooldownPlayers.containsKey(player.getUniqueId())) {
			long time = PitSimServer.cooldownPlayers.get(player.getUniqueId());

			if(time + COOLDOWN_SECONDS * 1000 < System.currentTimeMillis()) {
				PitSimServer.cooldownPlayers.remove(player.getUniqueId());
			} else {
				if(event.getMessage().toLowerCase().startsWith("/play")) return;
				event.setCancelled(true);
				AOutput.error(player, "You may not use that command at this time.");
				return;
			}
		}

		if(PitSimServer.guildCooldown.containsKey(player.getUniqueId())) {
			long time = PitSimServer.guildCooldown.get(player.getUniqueId());

			if(time + COOLDOWN_SECONDS * 1000 < System.currentTimeMillis()) {
				PitSimServer.guildCooldown.remove(player.getUniqueId());
			} else {
				if(event.getMessage().toLowerCase().startsWith("/play")) return;
				event.setCancelled(true);
				AOutput.error(player, "You may not use that command at this time.");
				return;
			}
		}
	}
}
