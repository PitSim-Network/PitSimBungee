package dev.wiji.instancemanager.discord;

import dev.wiji.instancemanager.BungeeMain;
import dev.wiji.instancemanager.ConfigManager;
import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.objects.PluginMessage;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DiscordManager implements EventListener, Listener {

	public static JDABuilder BUILDER;
	public static net.dv8tion.jda.api.JDA JDA;
	public static List<DiscordCommand> commands = new ArrayList<>();
	public static String prefix = ".";

	public static Guild MAIN_GUILD;
	public static Guild PRIVATE_GUILD;

	public static final String DISCORD_TABLE = "DiscordAuthentication";

	public DiscordManager() {
		System.out.println("Discord bot loading");
		BUILDER = JDABuilder.createDefault("***REMOVED***");
		try {
			BUILDER.setMemberCachePolicy(MemberCachePolicy.ALL);
			BUILDER.enableIntents(GatewayIntent.GUILD_MEMBERS);
			BUILDER.addEventListeners(this);
			JDA = BUILDER.build();
			JDA.awaitReady();
		} catch(LoginException | InterruptedException e) {
			e.printStackTrace();
		}

		MAIN_GUILD = JDA.getGuildById(Constants.MAIN_GUILD_ROLE_ID);
		PRIVATE_GUILD = JDA.getGuildById(Constants.PRIVATE_GUILD_ROLE_ID);
	}

	public static void registerCommand(DiscordCommand command) {

		commands.add(command);
	}

	public static void disable() {
		if(ConfigManager.isDev()) return;
		JDA.shutdownNow();
	}

	public static boolean isBoosting(UUID uuid) {
		DiscordUser user = getUser(uuid);
		if(user == null) return false;
		long id = user.discordID;
		Member member = MAIN_GUILD.retrieveMemberById(id).complete();

		return member.getRoles().contains(InGameNitro.nitroRole);
	}

	public static void onMessage(GuildMessageReceivedEvent event) {
		Message message = event.getMessage();
		if(!message.getContentRaw().startsWith(prefix)) return;

		String content = message.getContentRaw().replaceFirst(prefix, "");
		List<String> args = new ArrayList<>(Arrays.asList(content.split(" ")));
		String command = args.remove(0).toLowerCase();

		for(DiscordCommand discordCommand : commands) {

			if(!discordCommand.command.equals(command) && !discordCommand.aliases.contains(command)) continue;

			discordCommand.execute(event, args);
			return;
		}
	}

	@Override
	public void onEvent(@NotNull GenericEvent event) {

		if(event instanceof ReadyEvent)
			System.out.println("Discord bot enabled");

		if(event instanceof GuildMessageReceivedEvent)
			onMessage((GuildMessageReceivedEvent) event);
	}

	public static Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String dbUrl = "jdbc:mysql://sql.pitsim.net:3306/s9_PlayerData";
			String username = "***REMOVED***";
			String password = "***REMOVED***";
			return DriverManager.getConnection(dbUrl, username, password);
		} catch(Exception ignored) {}
		return null;
	}

	public static void createTable(Connection connection) throws SQLException, ClassNotFoundException {
		Statement stmt = connection.createStatement();

		// Create the table
		String createTableSQL = "CREATE TABLE " + DISCORD_TABLE + " (" +
				"uuid VARCHAR(36) PRIMARY KEY, " +
				"discord_id BIGINT NOT NULL, " +
				"access_token VARCHAR(50), " +
				"refresh_token VARCHAR(50), " +
				"last_refresh BIGINT NOT NULL, " +
				"last_link BIGINT NOT NULL, " +
				"last_boosting_claim BIGINT NOT NULL)";
		stmt.executeUpdate(createTableSQL);

		// Close the statement and connection
		stmt.close();
		connection.close();
	}

	public static DiscordUser getUser(UUID uuid) {
		Connection connection = getConnection();
		assert connection != null;

		try {
			String sql = "SELECT discord_id, access_token, refresh_token, last_refresh, last_link, last_boosting_claim FROM " + DISCORD_TABLE + " WHERE uuid = ?";
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, uuid.toString());
			ResultSet rs = stmt.executeQuery();

			if(rs.next()) {
				long id = rs.getLong("discord_id");
				String access = rs.getString("access_token");
				String refresh = rs.getString("refresh_token");
				long refreshTime = rs.getLong("last_refresh");
				long lastLink = rs.getLong("last_link");
				long claim = rs.getLong("last_boosting_claim");

				return new DiscordUser(uuid, id, access, refresh, refreshTime, lastLink, claim);
			} else return null;
		} catch(SQLException e) {
			e.printStackTrace();
		}

		try {
			connection.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static DiscordUser getUser(long discordID) {
		Connection connection = getConnection();
		assert connection != null;

		try {
			String sql = "SELECT uuid, access_token, refresh_token, last_refresh, last_link, last_boosting_claim FROM " + DISCORD_TABLE + " WHERE discord_id = ?";
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setLong(1, discordID);
			ResultSet rs = stmt.executeQuery();

			if(rs.next()) {
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				String access = rs.getString("access_token");
				String refresh = rs.getString("refresh_token");
				long refreshTime = rs.getLong("last_refresh");
				long lastLink = rs.getLong("last_link");
				long claim = rs.getLong("last_boosting_claim");

				return new DiscordUser(uuid, discordID, access, refresh, refreshTime, lastLink, claim);
			} else return null;
		} catch(SQLException e) {
			e.printStackTrace();
		}

		try {
			connection.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void populateQueue() {
		Connection connection = getConnection();
		assert connection != null;

		try {
			String sql = "SELECT uuid FROM " + DISCORD_TABLE;
			PreparedStatement stmt = connection.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();

			while(rs.next()) {
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				AuthenticationManager.queuedUsers.add(uuid);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}

		try {
			connection.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();
		if(strings.isEmpty()) return;

		if(!strings.get(0).equals("BOOSTER_CLAIM")) return;
		String serverName = strings.get(1);
		String playerUUID = strings.get(2);

		new PluginMessage().writeString("BOOSTER_CLAIM")
				.writeString(playerUUID)
				.writeBoolean(isBoosting(UUID.fromString(playerUUID)))
				.addServer(BungeeMain.INSTANCE.getProxy().getServerInfo(serverName))
				.send();
	}
}
