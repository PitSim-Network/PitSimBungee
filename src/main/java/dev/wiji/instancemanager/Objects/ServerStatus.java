package dev.wiji.instancemanager.Objects;

public enum ServerStatus {
	RUNNING,
	RESTARTING_INITIAL,
	RESTARTING_FINAL,
	SHUTTING_DOWN_INITIAL,
	SHUTTING_DOWN_FINAL,
	OFFLINE;

	public boolean isOnline() {
		return this == RUNNING || this == RESTARTING_INITIAL || this == SHUTTING_DOWN_INITIAL;
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
