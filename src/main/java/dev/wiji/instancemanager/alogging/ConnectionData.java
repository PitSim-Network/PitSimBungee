package dev.wiji.instancemanager.alogging;

import dev.wiji.instancemanager.SQL.Field;
import dev.wiji.instancemanager.SQL.SQLTable;
import dev.wiji.instancemanager.SQL.TableManager;
import dev.wiji.instancemanager.pitsim.IdentificationManager;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConnectionData {
	public Map<String, PlayerConnectionData> playerConnectionMap = new HashMap<>();


	public ConnectionData() {
		SQLTable table = TableManager.getTable(IdentificationManager.TABLE_NAME);
		if(table == null) throw new RuntimeException("Table not found");

		ResultSet rs = table.selectRow(
				new Field("uuid"),
				new Field("username"),
				new Field("initial_join_domain")
		);

		try {
			while (rs.next()) {
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				String username = rs.getString("username");
				String domain = rs.getString("initial_join_domain");

				playerConnectionMap.put(uuid.toString(), new PlayerConnectionData(username, domain));
			}

			rs.close();
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