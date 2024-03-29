package net.pitsim.bungee.guilds.events;

import net.pitsim.bungee.guilds.controllers.objects.Guild;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GuildChatEvent extends GuildEvent {
	private ProxiedPlayer player;
	private String message;

	public GuildChatEvent(Guild guild, ProxiedPlayer player, String message) {
		super(guild);
		this.player = player;
		this.message = message;
	}

	public ProxiedPlayer getPlayer() {
		return player;
	}

	public String getMessage() {
		return message;
	}
}