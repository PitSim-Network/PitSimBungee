package dev.wiji.instancemanager.guilds.events;

import dev.wiji.instancemanager.guilds.controllers.objects.Guild;

public class GuildDisbandEvent extends GuildEvent {
	public GuildDisbandEvent(Guild guild) {
		super(guild);
	}
}
