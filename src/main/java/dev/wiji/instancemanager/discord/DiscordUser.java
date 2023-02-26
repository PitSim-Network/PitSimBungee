package dev.wiji.instancemanager.discord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class DiscordUser {
	public UUID uuid;
	public long discordID;
	public String accessToken;
	public String refreshToken;
	public long lastRefresh;
	public long lastBoostingClaim;

	public static final String DISCORD_TABLE = DiscordManager.DISCORD_TABLE;

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


	public void save() {
		try {
			String sql = "INSERT INTO " + DISCORD_TABLE + " (uuid, discord_id, access_token, refresh_token, last_boosting_claim)" +
					" VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE uuid = ?, discord_id = ?, access_token, refresh_token, last_boosting_claim";

			Connection connection = DiscordManager.getConnection();
			assert connection != null;

			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, uuid.toString());
			stmt.setLong(2, discordID);
			stmt.setString(3, accessToken);
			stmt.setString(4, refreshToken);
			stmt.setString(5, );
			stmt.executeUpdate();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}

		try {
			connection.close();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
