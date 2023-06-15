package net.pitsim.bungee.aserverstatistics;

import net.pitsim.bungee.SQL.Constraint;
import net.pitsim.bungee.SQL.SQLTable;
import net.pitsim.bungee.SQL.TableManager;
import net.pitsim.bungee.SQL.Value;
import net.pitsim.bungee.events.MessageEvent;
import net.pitsim.bungee.misc.AOutput;
import net.pitsim.bungee.pitsim.PitEnchant;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class StatisticsManager implements Listener {
	public static final List<StatisticDataChunk> queuedChunks = new ArrayList<>();
	public static final String TABLE_NAME = "enchant_statistics";
	public static final long MAX_TIME = 1000L * 60 * 60 * 24 * 30;

	public StatisticsManager() {
		new Thread(() -> {
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
		SQLTable table = TableManager.getTable(TABLE_NAME);
		if(table == null) throw new RuntimeException("Table not found");

		ResultSet rs = table.selectRow(new Constraint("category", category.name()));

		try {
			callback.accept(rs);
			rs.close();
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void insertChunk(StatisticDataChunk dataChunk) {
		AOutput.log("Inserting data to: " + TABLE_NAME);
		SQLTable table = TableManager.getTable(TABLE_NAME);
		if(table == null) throw new RuntimeException("Table not found");

		for(StatisticDataChunk.Record record : dataChunk.records) {
			List<String> hits = new ArrayList<>();
			for (Map.Entry<String, Integer> entry : record.getHitsWithEnchant().entrySet()) {
				int hitValue = entry.getValue();
				String hitString = hitValue != 0 ? String.valueOf(hitValue) : "NULL";
				hits.add(hitString);
			}
			String enchantRefName = record.getEnchantRefName() == null ? "NULL" : "'" + record.getEnchantRefName() + "'";

			List<Value> values = new ArrayList<>();
			values.add(new Value("date", dataChunk.getStartTime()));
			values.add(new Value("enchant", enchantRefName));
			values.add(new Value("category", record.getCategory()));
			values.add(new Value("total_hits", record.getTotalHits()));

			for(int i = 0; i < PitEnchant.getAllRefNames().size(); i++) {
				values.add(new Value(PitEnchant.getAllRefNames().get(i), hits.get(i)));
			}

			table.insertRow(values.toArray(new Value[0]));
		}
	}

	public static void deleteOldRows() {
		AOutput.log("Removing old enchant rows from: " + TABLE_NAME);
		SQLTable table = TableManager.getTable(TABLE_NAME);
		if(table == null) throw new RuntimeException("Table not found");

		table.deleteRow(
				new Constraint("date + " + MAX_TIME, System.currentTimeMillis(), "<")
		);
	}


	public static boolean tableExists(String tableName) {
		SQLTable table = TableManager.getTable(tableName);
		return table != null;
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}
