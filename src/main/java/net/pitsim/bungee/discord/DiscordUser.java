package net.pitsim.bungee.discord;

import io.mokulu.discord.oauth.DiscordAPI;
import io.mokulu.discord.oauth.model.User;
import net.pitsim.bungee.ConfigManager;
import net.pitsim.bungee.misc.AOutput;
import okhttp3.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DiscordUser {
	public UUID uuid;
	public long discordID;
	public String accessToken;
	public String refreshToken;
	public long lastRefresh;
	public long lastLink;
	public long lastBoostingClaim;

	public static final String DISCORD_TABLE = DiscordManager.DISCORD_TABLE;

//	initial construction
	public DiscordUser(UUID uuid, long discordID, String accessToken, String refreshToken) {
		this(uuid, discordID, accessToken, refreshToken, System.currentTimeMillis(), System.currentTimeMillis(), 0);
	}

//	loading from db
	public DiscordUser(UUID uuid, long discordID, String accessToken, String refreshToken, long lastRefresh, long lastLink, long lastBoostingClaim) {
		this.uuid = uuid;
		this.discordID = discordID;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.lastRefresh = lastRefresh;
		this.lastLink = lastLink;
		this.lastBoostingClaim = lastBoostingClaim;
	}

	public boolean wasAuthenticatedRecently() {
		if(AuthenticationManager.recentlyAuthenticatedUserMap.containsKey(uuid)) return true;
		try {
			DiscordAPI api = new DiscordAPI(accessToken);
			User user = api.fetchUser();
			AuthenticationManager.recentlyAuthenticatedUserMap.put(uuid, user);
		} catch(IOException exception) {
			exception.printStackTrace();
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

	public void remove() {
		Connection connection = DiscordManager.getConnection();
		assert connection != null;

		String sql = "DELETE FROM " + DISCORD_TABLE + " WHERE uuid = ?";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, uuid.toString());
			stmt.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}

		try {
			connection.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}

		AuthenticationManager.queuedUsers.remove(uuid);
	}

	public void save() {
		Connection connection = DiscordManager.getConnection();

		try {
			String sql = "INSERT INTO " + DISCORD_TABLE + " (uuid, discord_id, access_token, refresh_token, last_refresh, last_link, last_boosting_claim)" +
					" VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE uuid = ?, discord_id = ?, access_token = ?, refresh_token = ?," +
					" last_refresh = ?, last_link = ?, last_boosting_claim = ?";

			assert connection != null;

			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, uuid.toString());
			stmt.setLong(2, discordID);
			stmt.setString(3, accessToken);
			stmt.setString(4, refreshToken);
			stmt.setLong(5, lastRefresh);
			stmt.setLong(6, lastLink);
			stmt.setLong(7, lastBoostingClaim);

			stmt.setString(8, uuid.toString());
			stmt.setLong(9, discordID);
			stmt.setString(10, accessToken);
			stmt.setString(11, refreshToken);
			stmt.setLong(12, lastRefresh);
			stmt.setLong(13, lastLink);
			stmt.setLong(14, lastBoostingClaim);
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

	public void joinDiscord() throws Exception {
		String requestUrl = "https://discord.com/api/guilds/" + Constants.MAIN_GUILD_ROLE_ID + "/members/" + discordID;

		List<String> roles = new ArrayList<>();
		roles.add(Constants.MEMBER_ROLE_ID + "");
		String requestBody = "{"
				+ "\"access_token\":\"" + accessToken + "\","
				+ "\"roles\":" + roles
				+ "}";

		OkHttpClient client = new OkHttpClient();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, requestBody);
		Request request = new Request.Builder()
				.url(requestUrl)
				.method("PUT", body)
				.addHeader("Content-Type", "application/json")
				.addHeader("Authorization", "Bot " + ConfigManager.get("discord-bot-token"))
				.build();

		Response response = client.newCall(request).execute();

		int responseCode = response.code();
		if(responseCode != 201 && responseCode != 204) {
			String errorMessage = response.body().string();
			AOutput.log("Error joining user: " + discordID);
			AOutput.log("Error message: " + errorMessage);
			throw new Exception("Unexpected response code: " + responseCode);
		}
	}
}
