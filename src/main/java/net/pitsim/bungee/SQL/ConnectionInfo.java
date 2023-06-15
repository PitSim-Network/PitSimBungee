package net.pitsim.bungee.SQL;

import net.pitsim.bungee.ConfigManager;

import java.sql.Connection;
import java.sql.SQLException;

public enum ConnectionInfo {


	DEVELOPMENT(get("sql-dev-url"), get("sql-dev-username"), get("sql-dev-password"), 1000L * 60 * 60 * 24 * 30),
	PLAYER_DATA(get("sql-data-url"), get("sql-data-username"), get("sql-data-password"), 1000L * 60 * 60 * 24 * 30),
	STATISTICS(get("sql-stats-url"), get("sql-stats-username"), get("sql-stats-password"), 1000L * 60 * 60 * 24 * 30),
	;

	 public final String URL;
	 public final String USERNAME;
	 public final String PASSWORD;
	 public final long MAX_TIME;


	 ConnectionInfo(String URL, String USERNAME, String PASSWORD, long MAX_TIME) {
		 this.URL = URL;
		 this.USERNAME = USERNAME;
		 this.PASSWORD = PASSWORD;
		 this.MAX_TIME = MAX_TIME;
	 }

	 public Connection getConnection() {
		 String URL = this.URL;
		 try { Class.forName("com.mysql.jdbc.Driver"); } catch(ClassNotFoundException e) { throw new RuntimeException(e); }
		 try { return java.sql.DriverManager.getConnection(URL, USERNAME, PASSWORD); } catch(SQLException e) { throw new RuntimeException(e); }
	 }

	 public static String get(String key) {
		return ConfigManager.get(key);
	 }
}
