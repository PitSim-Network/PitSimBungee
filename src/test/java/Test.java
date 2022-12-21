import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Test {

	public static void main(String[] args) {
		init();
	}

	public static String ORIGINAL_TABLE = "de.bytecraft_software.PlayerStatus.Player";
	public static String NEW_TABLE = "PlayerData";

	public static void init() {
		Connection connection = getConnection();
		try {
			assert connection != null;
			createTable(connection);
			transferColumns(connection);
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}
	}


	public static void transferColumns(Connection conn) throws SQLException {
		// Check the number of columns in each table
		ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + ORIGINAL_TABLE);
		int numColumns1 = rs1.getInt(1);
		ResultSet rs2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + NEW_TABLE);
		int numColumns2 = rs2.getInt(1);
		if (numColumns1 < 2 || numColumns2 < 2) {
			throw new SQLException("Both tables must have at least two columns");
		}

		// Transfer the data
		String sql = "INSERT INTO " + NEW_TABLE + " (username, uuid) SELECT PlayerUUID, PlayerName FROM " + ORIGINAL_TABLE;
		conn.createStatement().executeUpdate(sql);
	}

	public static void createTable(Connection conn) throws SQLException {
		String sql = "CREATE TABLE " + NEW_TABLE + " (username VARCHAR(255), uuid CHAR(36))";
		conn.createStatement().executeUpdate(sql);
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
}
