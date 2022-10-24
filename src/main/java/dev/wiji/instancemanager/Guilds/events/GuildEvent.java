package dev.wiji.instancemanager.Guilds.events;

import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;

public class GuildEvent extends ArcticGuildEvent {
	private Guild guild;

	public GuildEvent(Guild guild) {
		this.guild = guild;
	}

	public Guild getGuild() {
		return guild;
	}
}
