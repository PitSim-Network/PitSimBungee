package dev.wiji.instancemanager.Guilds.events;

import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GuildDepositEvent extends GuildEvent {
	private ProxiedPlayer player;
	private int amount;

	public GuildDepositEvent(ProxiedPlayer player, Guild guild, int amount) {
		super(guild);
		this.player = player;
		this.amount = amount;
	}

	public ProxiedPlayer getPlayer() {
		return player;
	}

	public int getAmount() {
		return amount;
	}
}
