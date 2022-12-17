package dev.wiji.instancemanager.objects;

import net.md_5.bungee.api.ChatColor;

public enum ServerStatus {
	STARTING(ChatColor.YELLOW),
	RUNNING(ChatColor.GREEN),
	RESTARTING_INITIAL(ChatColor.GOLD),
	RESTARTING_FINAL(ChatColor.RED),
	SHUTTING_DOWN_INITIAL(ChatColor.GOLD),
	SHUTTING_DOWN_FINAL(ChatColor.RED),
	SUSPENDED(ChatColor.YELLOW),
	OFFLINE(ChatColor.RED);


	public final ChatColor color;

	ServerStatus(ChatColor color) {
		this.color = color;
	}

	public boolean isOnline() {
		return this == RUNNING || this == RESTARTING_INITIAL || this == SHUTTING_DOWN_INITIAL || this == SUSPENDED;
	}

	public boolean isShuttingDown() {
		return this == SHUTTING_DOWN_INITIAL || this == SHUTTING_DOWN_FINAL;
	}

	public boolean isRestarting() {
		return this == RESTARTING_INITIAL || this == RESTARTING_FINAL;
	}

	public boolean isQueueable() {
		return this == RUNNING;
	}
}
