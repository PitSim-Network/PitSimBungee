package net.pitsim.bungee.alogging;

import dev.wiji.instancemanager.SQL.Field;
import dev.wiji.instancemanager.SQL.SQLTable;
import dev.wiji.instancemanager.SQL.TableManager;
import dev.wiji.instancemanager.pitsim.IdentificationManager;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConnectionData {
	public Map<String, PlayerConnectionData> playerConnectionMap = new HashMap<>();


	public ConnectionData() {

		try {
			Connection connection = IdentificationManager.getConnection();
			assert connection != null;
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT uuid, username, initial_join_domain FROM " + IdentificationManager.NEW_TABLE);

			while (rs.next()) {
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				String username = rs.getString("username");
				String domain = rs.getString("initial_join_domain");

				playerConnectionMap.put(uuid.toString(), new PlayerConnectionData(username, domain));
			}

			rs.close();
			stmt.close();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static class PlayerConnectionData {
		public String name;
		public String host;

		public PlayerConnectionData(String name, String host) {
			this.name = name;
			this.host = host;
		}
	}
}