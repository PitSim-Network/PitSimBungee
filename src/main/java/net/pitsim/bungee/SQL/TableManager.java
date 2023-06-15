package net.pitsim.bungee.SQL;

import net.pitsim.bungee.pitsim.PitEnchant;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TableManager {
	private static final List<SQLTable> tables = new ArrayList<>();
	public static int openResultSets = 0;
	public static int totalResultSets = 0;

	public static void registerTables() {

		new SQLTable(ConnectionInfo.DEVELOPMENT, "development",
				new TableStructure(
						new TableColumn(String.class, "uuid"),
						new TableColumn(Integer.class, "xp"),
						new TableColumn(Long.class, "last_login"),
						new TableColumn(Boolean.class, "is_staff"),
						new TableColumn(Integer.class, "souls")
				));

		new SQLTable(ConnectionInfo.PLAYER_DATA, "PlayerInfo",
				new TableStructure(
						new TableColumn(String.class, "uuid", false, true),
						new TableColumn(String.class, "username", true),
						new TableColumn(Long.class, "last_join", true),
						new TableColumn(String.class, "initial_join_domain", true)
				));

		new SQLTable(ConnectionInfo.PLAYER_DATA, "DiscordAuthentication",
				new TableStructure(
						new TableColumn(String.class, "uuid", false, true),
						new TableColumn(Long.class, "discord_id", true),
						new TableColumn(String.class, "access_token"),
						new TableColumn(String.class, "refresh_token"),
						new TableColumn(Long.class, "last_refresh", true),
						new TableColumn(Long.class, "last_link", true),
						new TableColumn(Long.class, "last_boosting_claim", true)
				));

		TableStructure structure = new TableStructure(
				new TableColumn(Long.class, "date"),
				new TableColumn(String.class, "enchant", false),
				new TableColumn(String.class, "category"),
				new TableColumn(Integer.class, "total_hits")
		);

		for(String enchantRefName : PitEnchant.getAllRefNames()) {
			structure.columns.add(new TableColumn(Integer.class, enchantRefName));
		}
		new SQLTable(ConnectionInfo.STATISTICS, "enchant_statistics", structure);
	}

	protected static void registerTable(SQLTable table) {
		tables.add(table);
	}

	public static SQLTable getTable(String tableName) {
		for(SQLTable table : tables) {
			if(table.tableName.equals(tableName)) return table;
		}
		return null;
	}
}
