package net.pitsim.bungee.pitsim;

import net.md_5.bungee.api.plugin.Listener;
import net.pitsim.bungee.SQL.*;
import net.pitsim.bungee.alogging.ConnectionData;
import net.pitsim.bungee.alogging.ConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

public class IdentificationManager implements Listener {

	public static final String TABLE_NAME = "PlayerInfo";

	public static void onLogin(UUID uuid, String name, String domain) {
		deleteRows(uuid, name);

		if(isUUIDPresent(uuid.toString())) updatePlayer(uuid, name, System.currentTimeMillis());
		else insertIntoTableInitial(uuid, name, System.currentTimeMillis(), domain);
	}

	public static void updatePlayer(UUID uuid, String username, long time) {
		SQLTable table = TableManager.getTable(TABLE_NAME);
		if(table == null) throw new RuntimeException("Table not found");

		table.updateRow(
				new Value("username", username),
				new Value("last_join", time),
				new Constraint("uuid", uuid.toString())
		);
	}

	public static void insertIntoTableInitial(UUID uuid, String username, long time, String domain) {
		SQLTable table = TableManager.getTable(TABLE_NAME);
		if(table == null) throw new RuntimeException("Table not found");

		table.insertRow(
				new Value("uuid", uuid.toString()),
				new Value("username", username),
				new Value("last_join", time),
				new Value("initial_join_domain", domain)
		);

		ConnectionManager.connectionData.playerConnectionMap.put(uuid.toString(), new ConnectionData.PlayerConnectionData(username, domain));
	}

	public static void deleteRows(UUID uuid, String username) {
		SQLTable table = TableManager.getTable(TABLE_NAME);
		if(table == null) throw new RuntimeException("Table not found");

		table.deleteRow(
				new Constraint("username", username),
				new Constraint("uuid", uuid.toString())
		);
	}

	public static String getUsername(UUID uuid) {
		SQLTable table = TableManager.getTable(TABLE_NAME);
		if(table == null) throw new RuntimeException("Table not found");

		ResultSet rs = table.selectRow(
				new Field("username"),
				new Constraint("uuid", uuid.toString())
		);

		try {
			if(rs.next()) {
				String s = rs.getString("username");
				rs.close();
				return s;
			}
			rs.close();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}

		return uuid.toString();
	}

	public static UUID getUUID(String username) {
		SQLTable table = TableManager.getTable(TABLE_NAME);
		if(table == null) throw new RuntimeException("Table not found");

		ResultSet rs = table.selectRow(
				new Field("uuid"),
				new Constraint("username", username.toUpperCase(Locale.ROOT))
		);

		try {
			if(rs.next()) {
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				rs.close();
				return uuid;
			}
			rs.close();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}

		return null;
	}

	public static boolean isUUIDPresent(String uuid) {
		SQLTable table = TableManager.getTable(TABLE_NAME);
		if(table == null) throw new RuntimeException("Table not found");

		ResultSet rs = table.selectRow(
				new Field("uuid"),
				new Constraint("uuid", uuid)
		);

		boolean isPresent;

		try {
			isPresent = rs.next();
			rs.close();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}

		return isPresent;
	}
}
