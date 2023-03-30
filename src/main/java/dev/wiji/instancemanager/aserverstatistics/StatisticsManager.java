package dev.wiji.instancemanager.aserverstatistics;

import dev.wiji.instancemanager.events.MessageEvent;
import dev.wiji.instancemanager.misc.AOutput;
import dev.wiji.instancemanager.misc.PrivateInfo;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class StatisticsManager implements Listener {
	public static StatisticsSpigotToProxyDataShare sharedData;
	public static final List<StatisticDataChunk> queuedChunks = new ArrayList<>();
	public static boolean isInitialized;

	public static final String URL = "jdbc:mysql://sql.pitsim.net:3306/s9_Statistics";
	public static final String USERNAME = "***REMOVED***";
	public static final String PASSWORD = PrivateInfo.STATISTICS_SQL_PASSWORD;
	public static final String TABLE_NAME = "enchant_statistics";
	public static final long MAX_TIME = 1000L * 60 * 60 * 24 * 30;

	public static void init() {
		if(isInitialized) return;
		isInitialized = true;
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

		if(strings.get(0).equals("STATISTICS_SPIGOT_TO_PROXY_DATA_SHARE")) {
			AOutput.log("Received statistics data share");
			strings.remove(0);
			sharedData = new StatisticsSpigotToProxyDataShare(event);
			init();
		} else if(strings.get(0).equals("SERVER_STATISTICS")) {
			AOutput.log("Received statistics chunk from server: " + event.getMessage().originServer);
			if(sharedData == null) throw new RuntimeException();
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
				for(Map.Entry<String, Integer> entry : record.getHitsWithEnchant().entrySet()) hits.add(entry.getValue() + "");
				String sql = "INSERT INTO " + TABLE_NAME + " (date, enchant, category, total_hits, " +
						String.join(", ", sharedData.enchantInfoMap.keySet()) +
						") VALUES (" +
						dataChunk.getStartTime() + ", " +
						"'" + record.getEnchantRefName() + "', " +
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
			for(String enchantRefName : sharedData.enchantInfoMap.keySet()) enchantColumns.add(enchantRefName + " INT");
			Statement stmt = connection.createStatement();
			String sql = "CREATE TABLE " + TABLE_NAME + " (" +
					"date LONG, " +
					"enchant VARCHAR(50), " +
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
