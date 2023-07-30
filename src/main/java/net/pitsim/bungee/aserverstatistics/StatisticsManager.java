package net.pitsim.bungee.aserverstatistics;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.pitsim.bungee.ConfigManager;
import net.pitsim.bungee.events.MessageEvent;
import net.pitsim.bungee.misc.AOutput;
import net.pitsim.bungee.pitsim.PitEnchant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class StatisticsManager implements Listener {
	public static final List<StatisticDataChunk> queuedChunks = new ArrayList<>();

	public static final String URL = ConfigManager.get("sql-stats-url");
	public static final String USERNAME = ConfigManager.get("sql-stats-username");
	public static final String PASSWORD = ConfigManager.get("sql-stats-password");
	public static final String TABLE_NAME = "enchant_statistics";
	public static final long MAX_TIME = 1000L * 60 * 60 * 24 * 30;

	public StatisticsManager() {
		new Thread(() -> {
			if(!tableExists(TABLE_NAME)) createTable();
			deleteOldRows();
			while(true) {
				if(queuedChunks.isEmpty()) {
					sleep(1000);
					continue;
				}

				StatisticDataChunk dataChunk = queuedChunks.remove(0);
				insertChunk(dataChunk);

				sleep(1000);
			}
		}).start();
	}

	@EventHandler
	public void onMessage(MessageEvent event) {
		List<String> strings = event.getMessage().getStrings();
		if(strings.isEmpty()) return;

		if(strings.get(0).equals("SERVER_STATISTICS")) {
			AOutput.log("Received statistics chunk from server: " + event.getMessage().originServer);
			strings.remove(0);
			StatisticDataChunk dataChunk = new StatisticDataChunk(event);
			queuedChunks.add(dataChunk);
		}
	}

	public static void queryByCategory(StatisticCategory category, Consumer<ResultSet> callback) {
		Connection connection = getConnection();
		try {
			String sql = "SELECT * FROM " + TABLE_NAME + " WHERE category = ?";
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, category.name());
			ResultSet rs = stmt.executeQuery();

			// call the callback function with the result set
			callback.accept(rs);

			rs.close();
			stmt.close();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				connection.close();
			} catch(SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void insertChunk(StatisticDataChunk dataChunk) {
		AOutput.log("Inserting data to: " + TABLE_NAME);
		Connection connection = getConnection();
		try {
			Statement stmt = connection.createStatement();
			for(StatisticDataChunk.Record record : dataChunk.records) {
				List<String> hits = new ArrayList<>();
				for (Map.Entry<String, Integer> entry : record.getHitsWithEnchant().entrySet()) {
					int hitValue = entry.getValue();
					String hitString = hitValue != 0 ? String.valueOf(hitValue) : "NULL";
					hits.add(hitString);
				}
				String enchantRefName = record.getEnchantRefName() == null ? "NULL" : "'" + record.getEnchantRefName() + "'";
				String sql = "INSERT INTO " + TABLE_NAME + " (date, enchant, category, total_hits, " +
						String.join(", ", PitEnchant.getAllRefNames()) +
						") VALUES (" +
						dataChunk.getStartTime() + ", " +
						enchantRefName + ", " +
						"'" + record.getCategory() + "', " +
						record.getTotalHits() + ", " +
						String.join(", ", hits) +
						")";
				stmt.addBatch(sql);
			}
			stmt.executeBatch();

			stmt.close();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
		try {
			connection.close();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void deleteOldRows() {
		AOutput.log("Removing old enchant rows from: " + TABLE_NAME);
		Connection connection = getConnection();
		try {
			Statement stmt = connection.createStatement();
			String sql = "DELETE FROM " + TABLE_NAME + " WHERE date + " + MAX_TIME + " < " + System.currentTimeMillis();
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		try {
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void createTable() {
		AOutput.log("Creating table: " + TABLE_NAME);
		Connection connection = getConnection();
		try {
			List<String> enchantColumns = new ArrayList<>();
			for(String enchantRefName : PitEnchant.getAllRefNames()) enchantColumns.add(enchantRefName + " INT");
			Statement stmt = connection.createStatement();
			String sql = "CREATE TABLE " + TABLE_NAME + " (" +
					"date LONG, " +
					"enchant VARCHAR(50) NULL, " +
					"category VARCHAR(50), " +
					"total_hits INT, " +
					String.join(", ", enchantColumns) +
					")";
			stmt.executeUpdate(sql);

			stmt.close();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
		try {
			connection.close();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean tableExists(String tableName) {
		Connection connection = getConnection();
		try {
			DatabaseMetaData metadata = connection.getMetaData();
			ResultSet tables = metadata.getTables(null, null, tableName, null);
			boolean exists = tables.next();
			tables.close();
			return exists;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static Connection getConnection() {
		try {
			return DriverManager.getConnection(URL, USERNAME, PASSWORD);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}
