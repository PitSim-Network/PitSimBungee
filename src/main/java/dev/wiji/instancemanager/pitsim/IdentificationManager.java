package dev.wiji.instancemanager.pitsim;

import com.sun.org.apache.bcel.internal.generic.NEW;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.ConnectException;
import java.sql.*;
import java.util.Objects;
import java.util.UUID;

public class IdentificationManager implements Listener {

	public static String NEW_TABLE = "PlayerData";

	@EventHandler
	public void onJoin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		String name = player.getName();

		try {
			Connection connection = getConnection();
			insertIntoTable(Objects.requireNonNull(connection), uuid, name);
			connection.close();
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	public void insertIntoTable(Connection conn, UUID uuid, String username) throws SQLException {
		String sql = "INSERT INTO " + NEW_TABLE + " (uuid, username) VALUES (?, ?)";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, uuid.toString());
		stmt.setString(2, username);
		stmt.execute();
	}

	public static Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String dbUrl = "jdbc:mysql://sql.pitsim.net:3306/s1_PlayerData";
			String username = "u1_tNdewbWGuJ";
			String password = "xH@ngjlP8imF@PY8pP@psvRV";
			return DriverManager.getConnection(dbUrl, username, password);
		} catch(Exception ignored) { };
		return null;
	}

	public static String getUsername(Connection conn, UUID uuid) throws SQLException {
		String sql = "SELECT username FROM " + NEW_TABLE + " WHERE uuid = ?";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, uuid.toString());
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			return rs.getString("username");
		}
		return null;
	}

	public static UUID getUuid(Connection conn, String username) throws SQLException {
		String sql = "SELECT uuid FROM " + NEW_TABLE + " WHERE UPPER(username) = ?";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, username);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			return UUID.fromString(rs.getString("uuid"));
		}
		return null;
	}

}
