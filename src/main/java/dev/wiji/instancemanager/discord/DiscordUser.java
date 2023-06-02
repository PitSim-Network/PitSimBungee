package dev.wiji.instancemanager.discord;

import dev.wiji.instancemanager.SQL.Constraint;
import dev.wiji.instancemanager.SQL.SQLTable;
import dev.wiji.instancemanager.SQL.TableManager;
import dev.wiji.instancemanager.SQL.Value;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.misc.PrivateInfo;
import io.mokulu.discord.oauth.DiscordAPI;
import io.mokulu.discord.oauth.model.User;
import okhttp3.*;

import java.io.IOException;
import java.sql.ResultSet;
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
		SQLTable table = TableManager.getTable(DISCORD_TABLE);
		if(table == null) throw new RuntimeException("Table not found");

		table.deleteRow(
				new Constraint("uuid", uuid.toString())
		);

		AuthenticationManager.queuedUsers.remove(uuid);
	}

	public void save() {
		SQLTable table = TableManager.getTable(DISCORD_TABLE);
		if(table == null) throw new RuntimeException("Table not found");

		ResultSet rs = table.selectRow(
				new Constraint("uuid", uuid.toString())
		);

		try {
			if(rs.next()) {
				table.updateRow(
						new Constraint("uuid", uuid.toString()),
						new Value("discord_id", discordID),
						new Value("access_token", accessToken),
						new Value("refresh_token", refreshToken),
						new Value("last_refresh", lastRefresh),
						new Value("last_link", lastLink),
						new Value("last_boosting_claim", lastBoostingClaim)
				);
			} else {
				table.insertRow(
						new Value("uuid", uuid.toString()),
						new Value("discord_id", discordID),
						new Value("access_token", accessToken),
						new Value("refresh_token", refreshToken),
						new Value("last_refresh", lastRefresh),
						new Value("last_link", lastLink),
						new Value("last_boosting_claim", lastBoostingClaim)
				);
			}

			rs.close();
		} catch(SQLException exception) {
			exception.printStackTrace();
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
				.addHeader("Authorization", "Bot " + PrivateInfo.BOT_TOKEN)
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
