package net.pitsim.bungee.guilds.events;

import net.pitsim.bungee.guilds.controllers.objects.Guild;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GuildCreateEvent extends GuildEvent {
	private ProxiedPlayer player;

	public GuildCreateEvent(ProxiedPlayer player, Guild guild) {
		super(guild);
		this.player = player;
	}

	public ProxiedPlayer getPlayer() {
		return player;
	}
}
