package dev.wiji.instancemanager.pitsim;

import dev.wiji.instancemanager.BungeeMain;

import java.sql.*;
import java.util.Objects;
import java.util.UUID;

public class DiscordVerification {




	public static long getDiscord(UUID playerUUID) {
		Connection connection = getConnection();
		assert connection != null;

		try {
			String sql = "SELECT discord_id FROM " + DISCORD_TABLE + " WHERE uuid = ?";
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, playerUUID.toString());
			ResultSet rs = stmt.executeQuery();

			if(rs.next()) {
				return rs.getLong("discord_id");
			} else return 0;
		} catch(SQLException e) {
			e.printStackTrace();
		}

		try {
			connection.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}

		return 0;
	}
}
