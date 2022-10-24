package dev.wiji.instancemanager.Guilds.events;

import dev.wiji.instancemanager.Guilds.controllers.objects.Guild;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;

public class GuildWithdrawalEvent extends GuildEvent implements Cancellable {
	private boolean cancelled;
	private ProxiedPlayer player;
	private int amount;

	public GuildWithdrawalEvent(ProxiedPlayer player, Guild guild, int amount) {
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

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
