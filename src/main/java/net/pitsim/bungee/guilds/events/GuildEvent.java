package net.pitsim.bungee.guilds.events;

import net.pitsim.bungee.guilds.controllers.objects.Guild;

public class GuildEvent extends ArcticGuildEvent {
	private Guild guild;

	public GuildEvent(Guild guild) {
		this.guild = guild;
	}

	public Guild getGuild() {
		return guild;
	}
}
