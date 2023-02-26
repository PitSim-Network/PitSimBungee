package dev.wiji.instancemanager.discord;

import java.util.UUID;

public class DiscordUser {
	public UUID uuid;
	public long discordID;
	public String accessToken;
	public String refreshToken;
	public long lastRefresh;
	public long lastBoostingClaim;

//	initial construction
	public DiscordUser(UUID uuid, long discordID, String accessToken, String refreshToken, long lastRefresh) {
		this(uuid, discordID, accessToken, refreshToken, lastRefresh, 0);
	}

//	loading from db
	public DiscordUser(UUID uuid, long discordID, String accessToken, String refreshToken, long lastRefresh, long lastBoostingClaim) {
		this.uuid = uuid;
		this.discordID = discordID;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.lastRefresh = lastRefresh;
		this.lastBoostingClaim = lastBoostingClaim;
	}

	public boolean isAuthenticated() {
		return false;
	}

	//	TODO: wiji implement
	public void save() {

	}
}
