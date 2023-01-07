package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.BungeeMain;

import java.sql.*;
import java.util.Objects;
import java.util.UUID;

public class DiscordVerification {

	public static final String DISCORD_TABLE = "DiscordLink";

	public static Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String dbUrl = "jdbc:mysql://sql.pitsim.net:3306/s1_PlayerData";
			String username = "u1_tNdewbWGuJ";
			String password = "xH@ngjlP8imF@PY8pP@psvRV";
			return DriverManager.getConnection(dbUrl, username, password);
		} catch(Exception ignored) {} ;
		return null;
	}

	public static boolean verify(String name, long discordID) {
		UUID uuid = BungeeMain.getUUID(name, false);
		if(uuid == null) return false;
		insert(Objects.requireNonNull(getConnection()), uuid, discordID);
		return true;
	}

	public static void insert(Connection connection, UUID uuid, long discordID) {
		try {
			String sql = "INSERT INTO " + DISCORD_TABLE + " (uuid, discord_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE uuid = ?, discord_id = ?";
			assert connection != null;
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, uuid.toString());
			stmt.setLong(2, discordID);
			stmt.setString(3, uuid.toString());
			stmt.setLong(4, discordID);
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

	public static long getDiscord(UUID playerUUID) {
		Connection connection = getConnection();
		assert connection != null;

		try {
			String sql = "SELECT discord_id FROM " + DISCORD_TABLE + " WHERE uuid = ?";
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, playerUUID.toString());
			ResultSet rs = stmt.executeQuery();

			if(rs.next()) {
				return rs.getLong("discord_id");
			} else return 0;
		} catch(SQLException e) {
			e.printStackTrace();
		}

		try {
			connection.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static boolean unverifyDiscord(long discordID) {
		Connection connection = getConnection();
		assert connection != null;

		try {
			String sql = "DELETE FROM " + DISCORD_TABLE + " WHERE discord_id = ?";
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setLong(1, discordID);
			stmt.execute();


			String confirm = "SELECT uuid FROM " + DISCORD_TABLE + " WHERE discord_id = ?";
			PreparedStatement confirmStmt = connection.prepareStatement(confirm);
			confirmStmt.setLong(1, discordID);
			ResultSet rs = confirmStmt.executeQuery();
			return rs.next();

		} catch(SQLException e) {
			e.printStackTrace();
		}

		try {
			connection.close();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}

		return false;
	}

	public static void createTable(Connection connection) throws SQLException, ClassNotFoundException {
		Statement stmt = connection.createStatement();

		// Create the table
		String createTableSQL = "CREATE TABLE " + DISCORD_TABLE + " (" +
				"uuid VARCHAR(36) PRIMARY KEY, " +
				"discord_id BIGINT NOT NULL)";
		stmt.executeUpdate(createTableSQL);

		// Close the statement and connection
		stmt.close();
		connection.close();
	}
}
