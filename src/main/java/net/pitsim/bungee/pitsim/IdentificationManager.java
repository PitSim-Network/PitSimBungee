package net.pitsim.bungee.pitsim;

import dev.wiji.instancemanager.SQL.*;
import dev.wiji.instancemanager.alogging.ConnectionData;
import dev.wiji.instancemanager.alogging.ConnectionManager;
import net.md_5.bungee.api.plugin.Listener;

import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IdentificationManager implements Listener {

	public static String NEW_TABLE = "PlayerInfo";

	public static void migrate() {
		Connection connection = getConnection();

		File file = new File(BungeeMain.INSTANCE.getDataFolder().getPath() + "/s1_PlayerData_PlayerData.csv");

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;

			List<UUID> migratedUUIDs = new ArrayList<>();

			while(true) {
				line = reader.readLine();
				if(line == null || line.isEmpty()) break;

				String[] split = line.split(",");
				assert connection != null;

				UUID uuid;
				String name = split[1];
				try {
					uuid = UUID.fromString(split[0]);
				} catch(Exception e) {
					uuid = UUID.fromString(split[1]);
					name = split[0];
				}

				if(migratedUUIDs.contains(uuid)) continue;
				migratedUUIDs.add(uuid);
				insertIntoTableInitial(connection, uuid, name, System.currentTimeMillis(), "mc.pitsim.net");
			}

			assert connection != null;
			connection.close();

		} catch(IOException | SQLException e) {
			e.printStackTrace();
		}
	}

	public static void onLogin(UUID uuid, String name, String domain) {
		Connection connection = getConnection();
		assert connection != null;

		try {
			deleteRows(connection, uuid, name);
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}

		try {
			if(isUUIDPresent(uuid.toString(), connection)) updatePlayer(connection, uuid, name, System.currentTimeMillis());
			else insertIntoTableInitial(connection, uuid, name, System.currentTimeMillis(), domain);
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}

		try {
			connection.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public static void updatePlayer(Connection conn, UUID uuid, String username, long time) throws SQLException {
		String sql = "UPDATE " + NEW_TABLE + " SET username = ?, last_join = ? WHERE uuid = ?";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, username);
		stmt.setLong(2, time);
		stmt.setString(3, uuid.toString());
		stmt.execute();
	}

	public static void insertIntoTableInitial(Connection conn, UUID uuid, String username, long time, String domain) throws SQLException {
		String sql = "INSERT INTO " + NEW_TABLE + " (uuid, username, last_join, initial_join_domain) VALUES (?, ?, ?, ?)";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, uuid.toString());
		stmt.setString(2, username);
		stmt.setLong(3, time);
		stmt.setString(4, domain);
		stmt.execute();

		ConnectionManager.connectionData.playerConnectionMap.put(uuid.toString(), new ConnectionData.PlayerConnectionData(username, domain));
	}

	public static void deleteRows(Connection conn, UUID uuid, String username) throws SQLException {
		String sql = "DELETE FROM " + NEW_TABLE + " WHERE username = ? AND uuid != ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, username);
			stmt.setString(2, uuid.toString());
			stmt.executeUpdate();
		}
	}


	public static Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String dbUrl = "jdbc:mysql://sql.pitsim.net:3306/s9_PlayerData";
			String username = "***REMOVED***";
			String password = PrivateInfo.PLAYER_DATA_SQL_PASSWORD;
			return DriverManager.getConnection(dbUrl, username, password);
		} catch(Exception ignored) {}
		throw new RuntimeException();
	}

	public static String getUsername(Connection conn, UUID uuid) {
		try {
			String sql = "SELECT username FROM " + NEW_TABLE + " WHERE uuid = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, uuid.toString());
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) return rs.getString("username");
		} catch(SQLException exception) {
			exception.printStackTrace();
		}
		return uuid.toString();
	}

	public static UUID getUuid(Connection conn, String username) throws SQLException {
		String sql = "SELECT uuid FROM " + NEW_TABLE + " WHERE UPPER(username) = ?";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, username);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			return UUID.fromString(rs.getString("uuid"));
		}
		return null;
	}

	public static boolean isUUIDPresent(String uuid, Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();

		// Select the UUID column from the users table
		String selectSQL = "SELECT UUID FROM " + NEW_TABLE + " WHERE uuid = '" + uuid + "'";
		ResultSet rs = stmt.executeQuery(selectSQL);

		// If the ResultSet is not empty, the UUID is already present
		boolean isPresent = rs.next();

		// Close the ResultSet, statement, and connection
		rs.close();
		stmt.close();

		return isPresent;
	}

	public static void createTable(Connection connection) throws SQLException, ClassNotFoundException {
		Statement stmt = connection.createStatement();

		// Create the table
		String createTableSQL = "CREATE TABLE " + NEW_TABLE + " (" +
				"uuid VARCHAR(36) PRIMARY KEY, " +
				"username VARCHAR(255) NOT NULL, " +
				"last_join BIGINT NOT NULL, " +
				"initial_join_domain VARCHAR(255) NOT NULL)";
		stmt.executeUpdate(createTableSQL);

		// Close the statement and connection
		stmt.close();
		connection.close();
	}

	public static void moveMalformedData(String oldTableName, String newTableName, Connection conn) throws SQLException, ClassNotFoundException {

		// Create a Statement object
		Statement stmt = conn.createStatement();

		// Select the UUID and username columns from the old table
		String selectSQL = "SELECT username, uuid FROM " + oldTableName + " WHERE username IS NOT NULL AND uuid IS NOT NULL";
		ResultSet rs = stmt.executeQuery(selectSQL);

		System.out.println("Size: " + rs.getFetchSize());
		// Insert the UUID and username into the new table
		while (rs.next()) {
			String uuid = rs.getString("uuid");
			String username = rs.getString("username");

			try {
				// Attempt to parse the UUID value
				UUID.fromString(uuid);

				// The UUID value is not valid, check if the username is already present in the UUID column of the new table
				Statement tryStmt = conn.createStatement();
				String selectSQL2 = "SELECT UUID FROM " + newTableName + " WHERE uuid = '" + uuid + "'";
				ResultSet rs2 = tryStmt.executeQuery(selectSQL2);
				if (!rs2.next()) {
					// The username is not present in the UUID column of the new table, insert the username and UUID into the new table in reverse order
					String insertSQL = "INSERT INTO " + newTableName + " (uuid, username, last_join, initial_join_domain) VALUES ('" + uuid + "', '" + username + "' , '" + 0 + "', '')";
					tryStmt.executeUpdate(insertSQL);
				}
				rs2.close();
			} catch (IllegalArgumentException e) {
				// The UUID value is not valid, check if the username is already present in the UUID column of the new table
				Statement tryStmt = conn.createStatement();
				String selectSQL2 = "SELECT UUID FROM " + newTableName + " WHERE uuid = '" + username + "'";
				ResultSet rs2 = tryStmt.executeQuery(selectSQL2);
				if (!rs2.next()) {
					// The username is not present in the UUID column of the new table, insert the username and UUID into the new table in reverse order
					String insertSQL = "INSERT INTO " + newTableName + " (uuid, username, last_join, initial_join_domain) VALUES ('" + username + "', '" + uuid + "' , '" + 0 + "', '')";
					tryStmt.executeUpdate(insertSQL);
				}
				rs2.close();
			}
		}

		// Close the ResultSet, statement, and connection
		rs.close();
		stmt.close();
		conn.close();
	}

	public static void updateDomainData(Connection conn, File jsonFile) {

		System.out.println("Migrating domain data!");

		Reader reader = null;
		try {
			reader = Files.newBufferedReader(jsonFile.toPath());
		} catch(IOException e) {
			e.printStackTrace();
		}

		Gson gson = new Gson();
		assert reader != null;
		OldConnectionData oldConnectionData = gson.fromJson(reader, OldConnectionData.class);
		for(Map.Entry<String, OldConnectionData.PlayerConnectionData> entry : oldConnectionData.playerConnectionMap.entrySet()) {
			String uuid = entry.getKey();
			String domain = entry.getValue().host;

			try {
				PreparedStatement stmt = conn.prepareStatement("UPDATE " + NEW_TABLE + " SET initial_join_domain = ? WHERE uuid = ?");
				stmt.setString(1, domain);
				stmt.setString(2, uuid);
				stmt.executeUpdate();
				stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		try {
			PreparedStatement stmt = conn.prepareStatement("UPDATE " + NEW_TABLE + " SET initial_join_domain = ? WHERE initial_join_domain = ''");
			stmt.setString(1, "mc.pitsim.net");
			stmt.executeUpdate();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
