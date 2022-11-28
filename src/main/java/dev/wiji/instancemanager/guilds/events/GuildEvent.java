package dev.wiji.instancemanager.guilds.events;

import dev.wiji.instancemanager.guilds.controllers.objects.Guild;

public class GuildEvent extends ArcticGuildEvent {
	private Guild guild;

	public GuildEvent(Guild guild) {
		this.guild = guild;
	}

	public Guild getGuild() {
		return guild;
	}
}
