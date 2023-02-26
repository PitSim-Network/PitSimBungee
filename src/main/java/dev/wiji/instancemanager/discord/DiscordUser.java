package dev.wiji.instancemanager.discord;

import java.util.UUID;

public class DiscordUser {
	public UUID uuid;
	public long discordID;
	public String accessToken;
	public String refreshToken;
	public long lastRefresh;
	public int boostingKeys;

//	initial construction
	public DiscordUser(UUID uuid, long discordID, String accessToken, String refreshToken, long lastRefresh) {
		this(uuid, discordID, accessToken, refreshToken, lastRefresh, 0);
	}

//	loading from db
	public DiscordUser(UUID uuid, long discordID, String accessToken, String refreshToken, long lastRefresh, int boostingKeys) {
		this.uuid = uuid;
		this.discordID = discordID;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.lastRefresh = lastRefresh;
		this.boostingKeys = boostingKeys;
	}

	public boolean isAuthenticated() {
		return false;
	}

	//	TODO: wiji implement
	public void save() {

	}
}
