package dev.wiji.instancemanager.discord;

import io.mokulu.discord.oauth.DiscordAPI;
import io.mokulu.discord.oauth.model.User;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

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

	public boolean wasAuthenticatedRecently() {
		if(AuthenticationManager.recentlyAuthenticatedUserMap.containsKey(uuid)) return true;
		try {
			DiscordAPI api = new DiscordAPI(accessToken);
			User user = api.fetchUser();
			AuthenticationManager.recentlyAuthenticatedUserMap.put(uuid, user);
		} catch(IOException exception) {
			return false;
		}
		return true;
	}

	public boolean isAuthenticated() {
		try {
			DiscordAPI api = new DiscordAPI(accessToken);
			api.fetchUser();
		} catch(IOException exception) {
			return false;
		}
		return true;
	}

	public void save() {
			Connection connection = DiscordManager.getConnection();

			try {
				String sql = "INSERT INTO " + DISCORD_TABLE + " (uuid, discord_id, access_token, refresh_token, last_boosting_claim)" +
						" VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE uuid = ?, discord_id = ?, access_token = ?, refresh_token = ?, last_refresh = ?, last_boosting_claim = ?";

				assert connection != null;

				PreparedStatement stmt = connection.prepareStatement(sql);
				stmt.setString(1, uuid.toString());
				stmt.setLong(2, discordID);
				stmt.setString(3, accessToken);
				stmt.setString(4, refreshToken);
				stmt.setLong(5, lastRefresh);
				stmt.setLong(6, lastBoostingClaim);

				stmt.setString(7, uuid.toString());
				stmt.setLong(8, discordID);
				stmt.setString(9, accessToken);
				stmt.setString(10, refreshToken);
				stmt.setLong(11, lastRefresh);
				stmt.setLong(12, lastBoostingClaim);
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
